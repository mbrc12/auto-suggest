package io.mbrc.autosuggest;

import io.mbrc.autosuggest.popmap.PopularityMap;
import io.mbrc.autosuggest.popmap.Suggestion;
import io.mbrc.autosuggest.popmap.Suggestions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.language.Soundex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class FuzzyCorrector {

    private final PopularityMap<String> fuzzyCorrectMap;
    private final Soundex soundex;

    private final AppConfig appConfig;

    @Autowired
    FuzzyCorrector (PopularityMap<String> fuzzyCorrectMap,
                    Soundex soundex,
                    AppConfig appConfig) {

        this.fuzzyCorrectMap = fuzzyCorrectMap;
        this.soundex = soundex;
        this.appConfig = appConfig;
    }

    public List<Suggestion<String>> correct (String word) {
        String encoded = soundex.encode(word);
        assert encoded.length() == 4;

        Suggestions<String> suggestions = fuzzyCorrectMap.getSuggestions(encoded);

        if (suggestions.getSuggestions().isEmpty())
            return Collections.emptyList();

        List<Suggestion<String>> results = new LinkedList<>();

        for (Suggestion<String> suggestion : suggestions.getSuggestions()) {

        }

        return results;
    }
}
