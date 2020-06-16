package io.mbrc.autosuggest.poptrie;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.LinkedList;
import java.util.ListIterator;

// TODO: Reanalyse later if an LinkedList is the correct implementation
@Data
public class Node<T> {

    @SerializedName("c")
    LinkedList<IntPair> completions;

    @SerializedName("p")
    final int parent;

    @SerializedName("s")
    int popularity;

    @SerializedName("e")
    T edgeFrom;

    Node (int parent, T edgeFrom, int startingPopularity) {
        this.completions = new LinkedList<>();
        this.edgeFrom = edgeFrom;
        this.parent = parent;
        this.popularity = startingPopularity;
    }



    synchronized void addCompletion (int val, int node, int maxRank) {
        IntPair paired = IntPair.of(val, node);
        boolean done = false;

        ListIterator<IntPair> iter = completions.listIterator();

        while (iter.hasNext() && !done) {
            IntPair item = iter.next();
            if (item.getRight() == node) {
                item.setLeft(val);
                done = true;
            }
        }

        if (!done) {
            completions.add(paired);
        }

        completions.sort(IntPair::reverseComparator);

        while (completions.size() > maxRank)
            completions.pollLast();
    }
}
