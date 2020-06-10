package io.mbrc.autosuggest.popmap;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class Suggestion<T> {
    @SerializedName("s")
    int popularity;

    @SerializedName("v")
    T item;

    public static int reverseComparator (Suggestion<?> item1, Suggestion<?> item2) {
        return Integer.compare(item2.getPopularity(), item1.getPopularity());
    }
}
