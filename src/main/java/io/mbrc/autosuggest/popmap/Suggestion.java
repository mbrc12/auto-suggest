package io.mbrc.autosuggest.popmap;

import com.google.gson.annotations.SerializedName;

public class Suggestion<T> {
    @SerializedName("s")
    int popularity;

    @SerializedName("v")
    T item;

    public Suggestion(int popularity, T item) {
        this.popularity = popularity;
        this.item = item;
    }

    public static int reverseComparator (Suggestion<?> item1, Suggestion<?> item2) {
        return Integer.compare(item2.getPopularity(), item1.getPopularity());
    }

    public int getPopularity() {
        return this.popularity;
    }

    public T getItem() {
        return this.item;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public void setItem(T item) {
        this.item = item;
    }
}
