package io.mbrc.autosuggest;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Predicate;


@Service
public class CompletionService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CompletionService.class);
    private static String delimiter = " ";

    private final ReadWriteLock readWriteLock;

    private final FuzzyCorrector fuzzyCorrector;
    private final WordCompleter wordCompleter;
    private final TagSuggestor tagSuggestor;
    private final Predicate<String> ignorableChecker;

    private final AppConfig appConfig;

    private final int maxCompletions;

    CompletionService (ReadWriteLock readWriteLock,
                       FuzzyCorrector fuzzyCorrector,
                       WordCompleter wordCompleter,
                       TagSuggestor tagSuggestor,
                       Predicate<String> ignorableChecker,
                       AppConfig appConfig) {

        this.readWriteLock = readWriteLock;

        this.fuzzyCorrector = fuzzyCorrector;
        this.wordCompleter = wordCompleter;
        this.tagSuggestor = tagSuggestor;
        this.ignorableChecker = ignorableChecker;

        this.appConfig = appConfig;
        this.maxCompletions = appConfig.getMaxCompletions();
    }

    public List<String> complete (String input) {
        readWriteLock.readLock().lock();
        List<String> results = generateCompletions(input);
        readWriteLock.readLock().unlock();
        return results;
    }

    private List<String> generateCompletions (String input_) {

        String input = input_.toLowerCase();

        log.info("Complete: {}", input);

        StringTokenizer tokenizer = new StringTokenizer(input);
        LinkedList<String> tokens = new LinkedList<>();

        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }


        if (tokens.size() > appConfig.getMaxTokensForSuggestion()) {
            return List.of(input); // Too many tokens, don't try to suggest
        }

        if (tokens.isEmpty()) {
            return List.of(input); // No completions
        }

        String last = tokens.pollLast();
        List<String> lastWordCompletions =
                ignorableChecker.test(last) ?
                List.of(last) :
                wordCompleter.complete(last);

        log.info("Split: {} {}", tokens, lastWordCompletions);

        ArrayList<List<String>> suggestionsForEachToken = new ArrayList<>();
        for (String token : tokens) {
            List<String> fc = fuzzyCorrector.correct(token);
            log.info("Corrected fuz: {}", fc);
            suggestionsForEachToken.add(fc);
        }
        suggestionsForEachToken.add(lastWordCompletions);

        ResultCollection completions = new ResultCollection();

        recurseAndAddTags(0, new LinkedList<>(), suggestionsForEachToken, completions);

        return completions.getResults();
    }


    // Note that this function tries out more stuff near the end of the suggestedTokens
    // list. This bias is intended, because they are most likely to influence the next
    // tokens that those at the start of the token list

    private void recurseAndAddTags (int index,
                                    LinkedList<String> current,
                                    ArrayList<List<String>> suggestedTokens,
                                    ResultCollection results) {
        if (index == suggestedTokens.size()) { // last position
            for (List<String> tags : tagSuggestor.suggest(current)) {

                // Completion size should not be lower than number of words typed
                if (tags.size() < suggestedTokens.size())
                    continue;

                String total = String.join(delimiter, tags);
                results.add(total);
                if (results.size() >= maxCompletions)
                    return;
            }
        } else {
            for (String suggested : suggestedTokens.get(index)) {
                current.add(suggested);
                recurseAndAddTags(index + 1, current, suggestedTokens, results);
                current.removeLast();
                if (results.size() >= maxCompletions)
                    return;
            }
        }
    }

    private static class ResultCollection {
        final List<String> results;
        final Set<String> resultsSet;

        ResultCollection () {
            results = new LinkedList<>();
            resultsSet = new HashSet<>();
        }

        void add (String result) {
            if (resultsSet.contains(result))
                return;
            results.add(result);
            resultsSet.add(result);
        }

        int size () {
            return results.size();
        }

        public List<String> getResults() {
            return this.results;
        }

        public Set<String> getResultsSet() {
            return this.resultsSet;
        }
    }

}
