package io.mbrc.autosuggest.popmap;

import com.google.gson.reflect.TypeToken;
import io.mbrc.autosuggest.kvstore.KVStore;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

import static io.mbrc.autosuggest.Util.*;

// The use of this structure is to keep a persisted
// collection of objects, which are hashed by a provided
// function. For each hash, we store the objects that were
// most hit, as suggestions. T should be both serializable
// and deserializable by Gson. Our only use-case is T = String.
// More specifically, T should be a value-class, like Pairs, etc.

// Implementation Note: As with PopularityTrie, this also syncs
// are writes to the structure.

/* TODO: However is the current impl, all reads are also synced. This can
    probably be improved.
 */

public class PopularityMap<T> {

    private final static int startingPopularity = 0;
    private final static String itemPrefix = "@";

    private final KVStore kvStore;
    private final String prefix;
    private final int occurrenceIncrement;
    private final int selectionIncrement;
    private final int maxRank;
    private final Function<T, String> hashFunction;

    private final Type suggestionsType;

    private final Object mutex = new Object();

    private PopularityMap (KVStore kvStore,
                           String prefix,
                           int occurrenceIncrement,
                           int selectionIncrement,
                           int maxRank,
                           Function<T, String> hashFunction,
                           Type suggestionsType) {
        this.kvStore = kvStore;
        this.prefix = prefix;
        this.occurrenceIncrement = occurrenceIncrement;
        this.selectionIncrement = selectionIncrement;
        this.maxRank = maxRank;
        this.hashFunction = hashFunction;

        this.suggestionsType = suggestionsType;
    }

    private void postConstruct () {

    }

    public static <T> PopularityMap<T> getInstance (KVStore kvStore,
                                                    String prefix,
                                                    int occurrenceIncrement,
                                                    int selectionIncrement,
                                                    int maxRank,
                                                    Function<T, String> hashFunction,
                                                    Type suggestionsType) {

        PopularityMap<T> popularityMap = new PopularityMap<>
                (kvStore, prefix, occurrenceIncrement, selectionIncrement, maxRank,
                        hashFunction, suggestionsType);
        popularityMap.postConstruct();
        return popularityMap;
    }

    private Optional<Suggestions<T>> getSuggestionsByHash (String hashKey) {
        return Optional.ofNullable(kvStore.query(hashKey, suggestionsType));
    }

    public Suggestions<T> getSuggestions (T item) {
        synchronized (mutex) {
            return getSuggestionsByHash(prefixed(hashFunction.apply(item)))
                    .orElse(new Suggestions<>());
        }
    }

    public Suggestions<T> getSuggestionsFromHash (String hash) {
        synchronized (mutex) {
            return getSuggestionsByHash(prefixed(hash)).orElse(new Suggestions<>());
        }
    }

    private Optional<Integer> getPopularity (T item) {
        Integer popularity = kvStore.query(prefixedItem(item), Integer.class);
        return Optional.ofNullable(popularity);
    }

    private void setPopularity (T item, int popularity) {
        kvStore.insert(prefixedItem(item), popularity);
    }

    private void setSuggestions (T item, Suggestions<T> suggestions) {
        kvStore.insert(prefixed(hashFunction.apply(item)), suggestions);
    }


    /* TODO: The hashes are being computed more times than required, but its slightly more */

    public void insert (T item, InsertType insertType) {
        synchronized (mutex) {
            final int increment = incrementOf(insertType);

            final Suggestions<T> suggestions = getSuggestions(item);

            getPopularity(item).ifPresentOrElse(
                    curPop -> {
                        int newPop = curPop + increment;
                        setPopularity(item, newPop);
                        suggestions.add(item, newPop, maxRank);
                    },
                    () -> {
                        int newPop = increment;
                        setPopularity(item, newPop);
                        suggestions.add(item, newPop, maxRank);
                    }
            );

            setSuggestions (item, suggestions);
        }
    }

    private String prefixed (String val) {
        return prefix + ":" + val;
    }

    private String prefixedItem (T item) {
        return prefixed(itemPrefix + ":" + item.toString());
    }


    private int incrementOf (InsertType insertType) {
        switch (insertType) {
            case SELECTION: return selectionIncrement;
            case OCCURRENCE: return occurrenceIncrement;
        }
        throw new RuntimeException("Cannot reach here as switch cases are exhaustive");
    }
}
