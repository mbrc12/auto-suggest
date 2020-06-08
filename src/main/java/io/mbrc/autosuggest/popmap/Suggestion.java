package io.mbrc.autosuggest.popmap;

import lombok.Value;

@Value
public class Suggestion<T> {
    int popularity;
    T item;

    public static int reverseComparator (Suggestion<?> item1, Suggestion<?> item2) {
        return Integer.compare(item2.getPopularity(), item1.getPopularity());
    }
}
