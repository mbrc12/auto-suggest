package io.mbrc.autosuggest.poptrie;

import lombok.Data;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Node<T> {
    ConcurrentHashMap<T, Integer> next;
    LinkedList<IntPair> completions;
    final int parent;
    int popularity;
    T edgeFrom;

    Node (int parent, T edgeFrom, int startingPopularity) {
        this.completions = new LinkedList<>();
        this.edgeFrom = edgeFrom;
        this.parent = parent;
        this.popularity = startingPopularity;
        this.next = new ConcurrentHashMap<>();
    }

    public void putNext (T edge, int node) {
        next.put(edge, node);
    }

    public Integer getNext (T edge) {
        return next.get(edge);
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
