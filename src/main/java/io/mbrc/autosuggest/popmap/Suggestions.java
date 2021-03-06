package io.mbrc.autosuggest.popmap;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.ListIterator;

public class Suggestions<T> {

    // List of suggestions sorted in descending order by popularity
    @SerializedName("s")
    private final LinkedList<Suggestion<T>> suggestions;

    Suggestions () {
        this.suggestions = new LinkedList<>();
    }

    public void add (T item, int popularity, int maxRank) {
        Suggestion<T> suggestion = new Suggestion<>(popularity, item);

        ListIterator<Suggestion<T>> iterator = suggestions.listIterator();
        boolean done = false;

        while (iterator.hasNext()) {
            Suggestion<T> current = iterator.next();
            if (current.getItem().equals(item)) {
                iterator.set(suggestion);
                done = true;
                break;
            }
        }

        if (!done) {
            suggestions.add(suggestion);
        }

        suggestions.sort(Suggestion::reverseComparator);

        while (suggestions.size() > maxRank) {
            suggestions.pollLast();
        }
    }

    public LinkedList<Suggestion<T>> getSuggestions() {
        return this.suggestions;
    }
}
