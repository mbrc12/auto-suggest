package io.mbrc.autosuggest;

import io.mbrc.autosuggest.poptrie.PopularityTrie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

import static io.mbrc.autosuggest.poptrie.PopularityTrieHelper.asCharacterList;
import static io.mbrc.autosuggest.poptrie.PopularityTrieHelper.asString;

@Slf4j
@Service
public class WordCompleter {

    private final PopularityTrie<Character> wordCompleteTrie;

    private final AppConfig appConfig;

    WordCompleter (PopularityTrie<Character> wordCompleteTrie,
                   AppConfig appConfig) {
        this.wordCompleteTrie = wordCompleteTrie;
        this.appConfig = appConfig;
    }

    public List<String> complete (String word) {
        final List<String> results = new LinkedList<>();

        wordCompleteTrie.completionsOfPath(asCharacterList(word))
                .forEach(completion -> results.add(asString(completion.getPath())));

        return results;
    }
}
