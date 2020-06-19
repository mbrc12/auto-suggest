package io.mbrc.autosuggest;

import io.mbrc.autosuggest.kvstore.KVStore;
import io.mbrc.autosuggest.popmap.PopularityMap;
import io.mbrc.autosuggest.poptrie.PopularityTrie;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.language.Soundex;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static io.mbrc.autosuggest.Util.*;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private Integer maxWordsInPhrase;
    private String ingestFinishSymbol;
    private Integer maxPhrases;

    private Integer maxFuzzyDistance;

    public Integer maxSubPhrases;
    public Integer maxTagSuggestions;

    public Integer maxTokensForSuggestion;
    public Integer maxCompletions;

    public @Bean
    ReadWriteLock readWriteLock () {
        return new ReentrantReadWriteLock(true);
    }

    public @Bean String splitDelimiters () {

        return ",.;'\"|:-!@_=\\[]{}()<>?~`&*=+/";

//        StringBuilder stringBuilder = new StringBuilder();
//        for (char c = 0; c < 255; c++) {
//            if (isAlphabet(c) || Character.isDigit(c))
//                continue;
//            stringBuilder.append(c);
//        }
//        return stringBuilder.toString();
    }

    public @Bean
    Function<String, String> hashFunction () {
        Soundex soundex = new Soundex();
        return string -> {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < string.length(); i++) {
                char current = string.charAt(i);
                if (isAlphabet(current))
                    builder.append(current);
            }

            return soundex.encode(builder.toString());
        };
    }

    public @Bean
    PopularityMap<String> fuzzyCorrectMap (KVStore kvStore, Function<String, String> hashFunction) {
        return PopularityMap.getInstance
                (kvStore, "fz-1", 1, 2, 2,
                        hashFunction, stringSuggestionsType);
    }

    public @Bean
    PopularityTrie<Integer> wordCompleteTrie (KVStore kvStore) {
        return PopularityTrie.getInstance
                (kvStore, "wc-1", 1, 2, 2,
                        intNodeType);
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

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                new ClassPathResource("low_info_words.txt").getInputStream()));

        while (true) {
            try {
                String token = reader.readLine().strip().toLowerCase();
                ignoredWords.add(token);
                log.debug("Ignored word: {}", token);
            } catch (Exception e) {
                break;
            }
        }
//        String contents = Files.readString(
//                Path.of(new ClassPathResource("low_info_words.txt")
//                        .getFile()
//                        .getAbsolutePath()));
//
//        StringTokenizer tokens = new StringTokenizer(contents);
//        while (tokens.hasMoreTokens()) {
//            ignoredWords.add(tokens.nextToken().toLowerCase());
//        }

        final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        return word -> {
            if (ignoredWords.contains(word)) return true;
            return pattern.matcher(word).matches();
        };
    }

}
