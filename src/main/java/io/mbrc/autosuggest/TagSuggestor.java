package io.mbrc.autosuggest;

import io.mbrc.autosuggest.poptrie.PopularityTrie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

import static io.mbrc.autosuggest.Util.*;

@Slf4j
@Component
public class TagSuggestor {

    private final PopularityTrie<String> tagSuggestTrie;

    private final AppConfig appConfig;

    @Autowired
    TagSuggestor (PopularityTrie<String> tagSuggestTrie,
                  AppConfig appConfig) {
        this.tagSuggestTrie = tagSuggestTrie;
        this.appConfig = appConfig;
    }

    // Return a list of <phrase = list of word> completions
    // given a phrase
    private List<List<String>> suggest (List<String> phrase) {
        int n = phrase.size();
        List<LinkedList<Integer>> combinations = orderedCombinationsUpto(rangeList(0, n),
                appConfig.getMaxWordsInPhrase());

        // Order by decreasing sum, see Notes.md for rationale.
        combinations.sort((List<Integer> X, List<Integer> Y) -> {
            int totalX = 0, totalY = 0;
            for (int e : X) totalX += e;
            for (int e : Y) totalY += e;
            return Integer.compare(totalY, totalX);
        });

        int subPhrasesLeft = appConfig.getMaxSubPhrases();

        final List<List<String>> result = new LinkedList<>();

        for (List<Integer> phraseIndices : combinations) {
            List<String> subPhrase = selectIndices(phrase, phraseIndices);

            tagSuggestTrie.completionsOfPath(subPhrase).forEach(
                    completion -> result.add(completion.getPath()));

            if (result.size() >= appConfig.getMaxTagSuggestions())
                break;

            subPhrasesLeft--;
            if (subPhrasesLeft <= 0) break;
        }

        return result;
    }
}
