package io.mbrc.autosuggest.popmap;

import io.mbrc.autosuggest.Util;
import io.mbrc.autosuggest.kvstore.KVStore;
import org.apache.commons.codec.language.Soundex;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import static io.mbrc.autosuggest.Util.stringSuggestionsType;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
@SpringBootTest
public class PopMapTest {

    private final static Soundex soundex = new Soundex();
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PopMapTest.class);

    private @Autowired
    KVStore kvStore;

    @Test
    public void popMap1 () {
        PopularityMap<String> popularityMap = PopularityMap.getInstance
                (kvStore, "pm-1", 1, 2, 4,
                        soundex::encode, stringSuggestionsType);
        popularityMap.insert("levenstein", Util.InsertType.OCCURRENCE);
        popularityMap.insert("levenshtein", Util.InsertType.OCCURRENCE);
        popularityMap.insert("levenshtein", Util.InsertType.SELECTION);

        Suggestions<String> suggestions = popularityMap.getSuggestions("levenstein");

        assertTrue(suggestions.getSuggestions().size() > 0);

        log.info("Suggestions: {}", suggestions.getSuggestions());
    }
}
