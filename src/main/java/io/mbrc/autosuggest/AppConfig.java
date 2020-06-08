package io.mbrc.autosuggest;

import io.mbrc.autosuggest.kvstore.KVStore;
import io.mbrc.autosuggest.popmap.PopularityMap;
import io.mbrc.autosuggest.poptrie.PopularityTrie;
import lombok.Data;
import org.apache.commons.codec.language.Soundex;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private Integer maxWordsInPhrase;
    private String ingestFinishSymbol;

    public @Bean Soundex soundex () {
        return new Soundex();
    }

    public @Bean
    PopularityMap<String> fuzzyCorrectMap (KVStore kvStore, Soundex soundex) {
        return PopularityMap.getInstance
                (kvStore, "fz-1", 1, 2, 3,
                        soundex::encode);
    }

    public @Bean
    PopularityTrie<Character> wordCompleteTrie (KVStore kvStore, Soundex soundex) {
        return PopularityTrie.getInstance
                (kvStore, "wc-1", 1, 2, 3);
    }

    public @Bean
    PopularityTrie<String> tagSuggestTrie (KVStore kvStore) {
        return PopularityTrie.getInstance
                (kvStore, "ts-1", 1, 2, 3);
    }
}
