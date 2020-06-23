package io.mbrc.autosuggest.poptrie;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

// TODO: Reanalyse later if an LinkedList is the correct implementation
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return parent == node.parent &&
                popularity == node.popularity &&
                Objects.equals(completions, node.completions) &&
                Objects.equals(edgeFrom, node.edgeFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(completions, parent, popularity, edgeFrom);
    }

    public LinkedList<IntPair> getCompletions() {
        return this.completions;
    }

    public int getParent() {
        return this.parent;
    }

    public int getPopularity() {
        return this.popularity;
    }

    public T getEdgeFrom() {
        return this.edgeFrom;
    }

    public void setCompletions(LinkedList<IntPair> completions) {
        this.completions = completions;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public void setEdgeFrom(T edgeFrom) {
        this.edgeFrom = edgeFrom;
    }
}
