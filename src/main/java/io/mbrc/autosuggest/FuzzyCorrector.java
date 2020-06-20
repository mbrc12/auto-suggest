package io.mbrc.autosuggest;

import io.mbrc.autosuggest.popmap.PopularityMap;
import io.mbrc.autosuggest.popmap.Suggestion;
import io.mbrc.autosuggest.popmap.Suggestions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static io.mbrc.autosuggest.Util.editDistance;
import static io.mbrc.autosuggest.Util.isEnglish;
import static io.mbrc.autosuggest.poptrie.PopularityTrieHelper.asIntegerList;

@Slf4j
@Service
public class FuzzyCorrector {

    private final PopularityMap<String> fuzzyCorrectMap;
    private final Function<String, String> hashFunction;

    private final AppConfig appConfig;

    @Autowired
    FuzzyCorrector (PopularityMap<String> fuzzyCorrectMap,
                    Function<String, String> hashFunction,
                    AppConfig appConfig) {

        this.fuzzyCorrectMap = fuzzyCorrectMap;
        this.hashFunction = hashFunction;
        this.appConfig = appConfig;
    }

    public List<String> correct (String word) {

        // Return only this word if the word is not in english character
        // set because cannot compute phonetic encoding effectively

        for (int codePoint : asIntegerList(word)) {
            if (!isEnglish(codePoint))
                return List.of(word);
        }

        String encoded = hashFunction.apply(word);
        log.debug("--> ? {}", encoded);

        Suggestions<String> suggestions = fuzzyCorrectMap.getSuggestionsFromHash(encoded);

        log.debug("Sugg: {}", suggestions);

        if (suggestions.getSuggestions().isEmpty())
            return Collections.emptyList();

        List<String> results = new LinkedList<>();
//        results.add(word);

        for (Suggestion<String> suggestion : suggestions.getSuggestions()) {
            String fixed = suggestion.getItem();
            log.debug(" Fixed: {}", fixed);
            if (fixed.equals(word))     // Some word is the same, then no need to correct
                return List.of(word);
            if (editDistance(fixed, word) <= appConfig.getMaxFuzzyDistance()) {
                results.add(fixed);
            }
        }

        return results;
    }
}
