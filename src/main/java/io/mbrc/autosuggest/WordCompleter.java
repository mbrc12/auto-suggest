package io.mbrc.autosuggest;

import io.mbrc.autosuggest.poptrie.PopularityTrie;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

import static io.mbrc.autosuggest.poptrie.PopularityTrieHelper.asIntegerList;
import static io.mbrc.autosuggest.poptrie.PopularityTrieHelper.asString;

@Service
public class WordCompleter {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(WordCompleter.class);
    private final PopularityTrie<Integer> wordCompleteTrie;

    private final AppConfig appConfig;

    WordCompleter (PopularityTrie<Integer> wordCompleteTrie,
                   AppConfig appConfig) {
        this.wordCompleteTrie = wordCompleteTrie;
        this.appConfig = appConfig;
    }

    public List<String> complete (String word) {
        final List<String> results = new LinkedList<>();

        wordCompleteTrie.completionsOfPath(asIntegerList(word))
                .forEach(completion -> results.add(asString(completion.getPath())));

        return results;
    }
}
