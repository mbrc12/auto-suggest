package io.mbrc.autosuggest;

import io.mbrc.autosuggest.kvstore.KVStore;
import io.mbrc.autosuggest.popmap.PopularityMap;
import io.mbrc.autosuggest.poptrie.PopularityTrie;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.mbrc.autosuggest.Util.*;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AppConfig.class);
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

    public @Bean
    PopularityMap<String> fuzzyCorrectMap (KVStore kvStore) {
        return PopularityMap.getInstance
                (kvStore, "fz-1", 1, 2, 2,
                        Services.hashFunction(), stringSuggestionsType);
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


    public Integer getMaxWordsInPhrase() {
        return this.maxWordsInPhrase;
    }

    public String getIngestFinishSymbol() {
        return this.ingestFinishSymbol;
    }

    public Integer getMaxPhrases() {
        return this.maxPhrases;
    }

    public Integer getMaxFuzzyDistance() {
        return this.maxFuzzyDistance;
    }

    public Integer getMaxSubPhrases() {
        return this.maxSubPhrases;
    }

    public Integer getMaxTagSuggestions() {
        return this.maxTagSuggestions;
    }

    public Integer getMaxTokensForSuggestion() {
        return this.maxTokensForSuggestion;
    }

    public Integer getMaxCompletions() {
        return this.maxCompletions;
    }

    public void setMaxWordsInPhrase(Integer maxWordsInPhrase) {
        this.maxWordsInPhrase = maxWordsInPhrase;
    }

    public void setIngestFinishSymbol(String ingestFinishSymbol) {
        this.ingestFinishSymbol = ingestFinishSymbol;
    }

    public void setMaxPhrases(Integer maxPhrases) {
        this.maxPhrases = maxPhrases;
    }

    public void setMaxFuzzyDistance(Integer maxFuzzyDistance) {
        this.maxFuzzyDistance = maxFuzzyDistance;
    }

    public void setMaxSubPhrases(Integer maxSubPhrases) {
        this.maxSubPhrases = maxSubPhrases;
    }

    public void setMaxTagSuggestions(Integer maxTagSuggestions) {
        this.maxTagSuggestions = maxTagSuggestions;
    }

    public void setMaxTokensForSuggestion(Integer maxTokensForSuggestion) {
        this.maxTokensForSuggestion = maxTokensForSuggestion;
    }

    public void setMaxCompletions(Integer maxCompletions) {
        this.maxCompletions = maxCompletions;
    }
}
