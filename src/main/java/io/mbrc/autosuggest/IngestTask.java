package io.mbrc.autosuggest;

import io.mbrc.autosuggest.popmap.PopularityMap;
import io.mbrc.autosuggest.poptrie.PopularityTrie;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Predicate;

import static io.mbrc.autosuggest.Util.*;
import static io.mbrc.autosuggest.poptrie.PopularityTrieHelper.asIntegerList;

@Slf4j
@Service
public class IngestTask {
    
    private final static String insertPrefix = "i:";
    private final static String updatePrefix = "u:";
    private final static String finishPrefix = "f:";

    private final ReadWriteLock readWriteLock;

    private final PopularityMap<String> fuzzyCorrectMap;
    private final PopularityTrie<Integer> wordCompleteTrie;
    private final PopularityTrie<String> tagSuggestTrie;
    private final Predicate<String> ignorableChecker;

    private final AppConfig config;
    private final BlockingDeque<String> queue;
    private final CountDownLatch finishLatch;

    private final String splitDelimiters;

    @Autowired
    IngestTask (ReadWriteLock readWriteLock,
                PopularityMap<String> fuzzyCorrectMap,
                PopularityTrie<Integer> wordCompleteTrie,
                PopularityTrie<String> tagSuggestTrie,
                Predicate<String> ignorableChecker,
                String splitDelimiters,
                AppConfig config) {

        this.readWriteLock = readWriteLock;

        this.fuzzyCorrectMap = fuzzyCorrectMap;
        this.wordCompleteTrie = wordCompleteTrie;
        this.tagSuggestTrie = tagSuggestTrie;
        this.ignorableChecker = ignorableChecker;

        this.config = config;
        this.queue = new LinkedBlockingDeque<>();

        this.finishLatch = new CountDownLatch(1);

        this.splitDelimiters = splitDelimiters;

        Thread workerThread = new Thread(this::worker);
        workerThread.start();
    }

    private void worker () {
        int count = 0;
        while (true) {
            try {
                String item = queue.takeFirst();

                log.info("Item #{}", ++count);

                Optional<String> content = splitPrefix(finishPrefix, item);
                if (content.isPresent()) {
                    finishLatch.countDown();
                    return;
                }

                readWriteLock.writeLock().lock();

                splitPrefix(insertPrefix, item).ifPresentOrElse(
                        // if present
                        data -> indexContent(data, InsertType.OCCURRENCE),

                        // otherwise
                        () -> splitPrefix(updatePrefix, item).ifPresent(
                                selection -> indexContent(selection, InsertType.SELECTION)));

                readWriteLock.writeLock().unlock();

            } catch (InterruptedException e) {
                log.error("Worker thread interrupted.");
                e.printStackTrace();
                return;
            }
        }
    }

    private void indexContent (String content, InsertType insertType) {
        List<String> words = splitToWords(content);
        if (words.isEmpty()) return;

        log.info("{} --> {}", content, words.toString());
        List<LinkedList<String>> phrases = computePhrases (words);

        words.forEach(word -> fuzzyCorrectMap.insert(word, insertType));

        words.forEach(
                word -> wordCompleteTrie.insert(asIntegerList(word), insertType));

        phrases.forEach(phrase -> tagSuggestTrie.insert(phrase, insertType));
    }

    public List<LinkedList<String>> computePhrases (List<String> words) {

        LinkedList<LinkedList<String>> phrases = new LinkedList<>();

        int N = words.size();

        int phrasesLeft = config.getMaxPhrases();

        for (int count = 1; count <= config.getMaxWordsInPhrase(); count++) {
            int currentSize = computeSize(N, count);
            if (currentSize <= phrasesLeft) {
                phrases.addAll(orderedGrams(words, count));
                phrasesLeft -= currentSize;
            } else {
                break;
            }
        }
        return phrases;
    }

    private List<String> splitToWords (String data) {
        StringTokenizer tokenizer = new StringTokenizer(data, splitDelimiters);
        List<String> words = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase();
            if (ignorableChecker.test(word))
                continue;
            words.add(word);
        }

        return words;
    }

    public void submit (String doc) {
        try {
            queue.putLast(prefixed(insertPrefix, doc));
        } catch (InterruptedException e) {
            log.info("Couldn't index document. Interrupted.");
            e.printStackTrace();
        }
    }

    public void selected (String selected) {
        try {
            queue.putLast(prefixed(updatePrefix, selected));
        } catch (InterruptedException e) {
            log.info("Couldn't update selection. Interrupted");
        }
    }

    @Synchronized
    public void shutdown () throws InterruptedException {
        log.info("Shutting down IngestTask..");
        queue.putLast(finishPrefix);
        finishLatch.await();
    }
}
