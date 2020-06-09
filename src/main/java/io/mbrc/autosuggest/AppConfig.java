package io.mbrc.autosuggest;

import com.google.gson.reflect.TypeToken;
import io.mbrc.autosuggest.kvstore.KVStore;
import io.mbrc.autosuggest.popmap.PopularityMap;
import io.mbrc.autosuggest.popmap.Suggestions;
import io.mbrc.autosuggest.poptrie.Node;
import io.mbrc.autosuggest.poptrie.PopularityTrie;
import lombok.Data;
import org.apache.commons.codec.language.Soundex;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static io.mbrc.autosuggest.Util.*;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private Integer maxWordsInPhrase;
    private String ingestFinishSymbol;

    private Integer maxFuzzyDistance;

    public Integer maxSubPhrases;
    public Integer maxTagSuggestions;

    public Integer maxTokensForSuggestion;
    public Integer maxCompletions;

    public @Bean
    Function<String, String> hashFunction () {
        Soundex soundex = new Soundex();
        return soundex::encode;
    }

    public @Bean
    PopularityMap<String> fuzzyCorrectMap (KVStore kvStore, Function<String, String> hashFunction) {
        return PopularityMap.getInstance
                (kvStore, "fz-1", 1, 2, 2,
                        hashFunction, stringSuggestionsType);
    }

    public @Bean
    PopularityTrie<Character> wordCompleteTrie (KVStore kvStore) {
        return PopularityTrie.getInstance
                (kvStore, "wc-1", 1, 2, 2,
                        charNodeType);
    }

    public @Bean
    PopularityTrie<String> tagSuggestTrie (KVStore kvStore) {
        return PopularityTrie.getInstance
                (kvStore, "ts-1", 1, 2, 2,
                        stringNodeType);
    }

    public @Bean
    Predicate<String> ignorableChecker () throws IOException {

        final Set<String> ignoredWords = new HashSet<>();

        String contents = Files.readString(
                Path.of(new ClassPathResource("low_info_words.txt")
                        .getFile()
                        .getAbsolutePath()));

        StringTokenizer tokens = new StringTokenizer(contents);
        while (tokens.hasMoreTokens()) {
            ignoredWords.add(tokens.nextToken().toLowerCase());
        }

        final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        return word -> {
            if (ignoredWords.contains(word)) return true;
            return pattern.matcher(word).matches();
        };
    }
}
