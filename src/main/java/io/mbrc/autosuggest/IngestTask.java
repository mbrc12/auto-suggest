package io.mbrc.autosuggest;

import io.mbrc.autosuggest.popmap.PopularityMap;
import io.mbrc.autosuggest.poptrie.PopularityTrie;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

import static io.mbrc.autosuggest.Util.orderedCombinationsUpto;
import static io.mbrc.autosuggest.poptrie.PopularityTrieHelper.asCharacterList;

@Slf4j
@Component
public class IngestTask {

    private final PopularityMap<String> fuzzyCorrectMap;
    private final PopularityTrie<Character> wordCompleteTrie;
    private final PopularityTrie<String> tagSuggestTrie;

    private final AppConfig config;
    private final BlockingDeque<String> queue;
    private final Set<String> ignoredWords;
    private final String delimiters;
    private final CountDownLatch finishLatch;

    @Autowired
    IngestTask (PopularityMap<String> fuzzyCorrectMap,
                PopularityTrie<Character> wordCompleteTrie,
                PopularityTrie<String> tagSuggestTrie,
                AppConfig config) throws IOException {

        this.fuzzyCorrectMap = fuzzyCorrectMap;
        this.wordCompleteTrie = wordCompleteTrie;
        this.tagSuggestTrie = tagSuggestTrie;

        this.config = config;
        this.queue = new LinkedBlockingDeque<>();
        this.ignoredWords = new HashSet<>();

        String contents = Files.readString(
                Path.of(new ClassPathResource("low_info_words.txt")
                        .getFile()
                        .getAbsolutePath()));

        StringTokenizer tokens = new StringTokenizer(contents);
        while (tokens.hasMoreTokens()) {
            ignoredWords.add(tokens.nextToken().toLowerCase());
        }

        this.delimiters = "!@#$%^&*()-=_+[]\\;',./{}|:\"<>?~`";
        this.finishLatch = new CountDownLatch(1);

        Thread workerThread = new Thread(this::worker);
        workerThread.start();
    }

    private void worker () {
        while (true) {
            try {
                String item = queue.takeFirst();
                if (item.equals(config.getIngestFinishSymbol())) {
                    finishLatch.countDown();
                    return;
                }
                Pair<List<String>, List<LinkedList<String>>> wordsAndphrases = analyzeToPhrases(item);
                List<String> words = wordsAndphrases.getFirst();
                List<LinkedList<String>> phrases = wordsAndphrases.getSecond();

                words.forEach(word -> fuzzyCorrectMap.insert(word, Util.InsertType.OCCURRENCE));

                words.forEach(
                        word -> wordCompleteTrie.insert(asCharacterList(word), Util.InsertType.OCCURRENCE));

                phrases.forEach(phrase -> tagSuggestTrie.insert(phrase, Util.InsertType.OCCURRENCE));

            } catch (InterruptedException e) {
                log.error("Worker thread interrupted.");
                e.printStackTrace();
                return;
            }
        }
    }

    // Remove all punctuation, tokenize into words, convert to lower
    // case, and return all possible k-letter phrases in-order.

    private Pair<List<String>, List<LinkedList<String>>> analyzeToPhrases (String data) {
        StringTokenizer tokenizer = new StringTokenizer(data, delimiters);
        List<String> words = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase();
            if (ignoredWords.contains(word))
                continue;

            words.add(word);
        }

        return Pair.of(
                words,
                orderedCombinationsUpto(words, config.getMaxWordsInPhrase()));
    }

    public void submit (String doc) throws InterruptedException {
        queue.putLast(doc);
    }

    @Synchronized
    public void shutdown () throws InterruptedException {
        log.info("Shutting down IngestTask..");
        queue.addLast(config.getIngestFinishSymbol());
        finishLatch.await();
    }

}
