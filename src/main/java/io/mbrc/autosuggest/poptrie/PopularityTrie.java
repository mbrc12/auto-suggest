package io.mbrc.autosuggest.poptrie;

import com.google.gson.reflect.TypeToken;
import io.mbrc.autosuggest.kvstore.KVStore;
import lombok.Synchronized;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;


import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.mbrc.autosuggest.Util.*;

// IMPORTANT: All T's used should have a good support for
// HashMap keys and Gson serialization. The expected
// candidates are T = Character / String. Also pass in a
// unique prefix, ensuring that no-other keys in your application
// ends up having this prefix.

// Implementation note: All write functions that are callable directly
// by the outside world, are synchronized on a shared mutex. This is
// to enable that these operations are sequential. As a result, we
// do not need to make the remaining operations synchronized.

/* TODO: Currently we sync all operations from the outside, essentially making this
    single threaded. But most reads can probably be done without blocking.
*/

/* TODO: Rewrite this whole thing using Optional<T> like PopularityMap
 */

@Slf4j
public class PopularityTrie <T> {

    private final static Integer startingPopularity = 0;
    private final static String currentIdKey = "currentId";

    private final int occurrenceIncrement;
    private final int selectionIncrement;
    private final int maxRank;
    private final Type nodeType;

    private final KVStore kvStore;
    private final String prefix;
    private Integer rootIdx;
    private AtomicInteger currentId;

    private final Object mutex = new Object();

    private PopularityTrie (KVStore kvStore,
                            String prefix,
                            int occurrenceIncrement,
                            int selectionIncrement,
                            int maxRank,
                            Type nodeType) {

        this.kvStore = kvStore;
        this.prefix = prefix;
        this.occurrenceIncrement = occurrenceIncrement;
        this.selectionIncrement = selectionIncrement;
        this.maxRank = maxRank;
        this.nodeType = nodeType;
    }

    // Check if the currentId has already been stored. If yes, then
    // this structure had already been present in the last iteration

    private void postConstruct () {
        Integer oldId = kvStore.query(prefixed(currentIdKey), Integer.class);

        if (oldId != null) {
            this.currentId = new AtomicInteger(oldId);
            this.rootIdx = kvStore.query(prefixed("root_val"), Integer.class);
            if (this.rootIdx == null) {
                throw new IllegalStateException("Found currentKey in Store but not root!");
            }
        } else {

            log.info("Initialize structure anew.");

            // Initialize the structure anew

            this.currentId = new AtomicInteger(0);
            kvStore.insert(prefixed(currentIdKey), 0);
            this.rootIdx = registerNewNode(-1, null);
            kvStore.insert(prefixed("root_val"), this.rootIdx);
        }
    }

    public static <T> PopularityTrie<T> getInstance(KVStore kvStore,
                                                    String prefix,
                                                    Integer occurrenceIncrement,
                                                    Integer selectionIncrement,
                                                    Integer maxRank,
                                                    Type nodeType) {
        PopularityTrie<T> tree = new PopularityTrie<>
                (kvStore, prefix, occurrenceIncrement, selectionIncrement, maxRank, nodeType);
        tree.postConstruct();
        return tree;
    }

    @Synchronized
    private int nextIndex () {
        int index = currentId.incrementAndGet();
        kvStore.insert(prefixed(currentIdKey), currentId.get());
        return index;
    }

    private int registerNewNode (int parent, T edgeFrom) {
        Node<T> node = new Node<>(parent, edgeFrom, startingPopularity);
        int index = nextIndex();
        kvStore.insert(prefixed(index), node);
        return index;
    }

    private Node<T> getNode (int index) {
        return kvStore.query(prefixed(index), nodeType);
    }

    private void putNode (Node<T> node, int index) {
        kvStore.insert(prefixed(index), node);
    }

    // Need not synchronise this, because the caller insert is synchronized on mutex
    // The returned pair is (Final Node's popularity, Final Node's index)
    private IntPair insert (ListIterator<T> iter, int nodeIdx, InsertType insertType) {
        Node<T> node = getNode(nodeIdx);

        if (node == null) {
            throw new IllegalStateException("Current node can never be null");
        }

        if (iter.hasNext()) {
            T edge = iter.next();
            Integer next = node.getNext(edge);

            if (next == null) {
                next = registerNewNode(nodeIdx, edge);
                node.putNext(edge, next);
            }

//            log.info("c = {}", node.getNext('c'));
//            log.info("dict : {}", node.getNext().toString());
//            log.info("dictkey: {}", node.getNext().keySet().toString());

            IntPair finalParams = insert(iter, next, insertType);
            int finalPopularity = finalParams.getLeft();
            int finalIdx = finalParams.getRight();

            node.addCompletion(finalPopularity, finalIdx, maxRank);

            putNode(node, nodeIdx);

            return finalParams;
        } else {
            node.setPopularity(node.getPopularity() + incrementOf(insertType));
            putNode(node, nodeIdx);
            return IntPair.of(node.getPopularity(), nodeIdx);
        }
    }

    public void insert (List<T> chain, InsertType type) {
        synchronized (mutex) {
            log.info("-- {}", chain);
            insert(chain.listIterator(), rootIdx, type);
        }
    }

    // Get the final node on the path, null if this path is not in trie
    private Integer getFinalNode (ListIterator<T> iter, int nodeIdx) {
        Node<T> node = getNode(nodeIdx);

        if (node == null) {
            throw new IllegalStateException("Current node can never be null");
        }

        if (iter.hasNext()) {
            T edge = iter.next();
            Integer next = node.getNext(edge);

            if (next == null) {
                return null;
            }

            return getFinalNode(iter, next);
        } else {
            return nodeIdx;
        }
    }

    private Integer getFinalNode (List<T> path) {
        return getFinalNode(path.listIterator(), rootIdx);
    }

    // Lock on mutex, before calling this method
    private List<T> pathOf (int index) {
        LinkedList<T> path = new LinkedList<>();
        Node<T> node = getNode(index);
        while (node.getParent() >= 0) {
            T edge = node.getEdgeFrom();
            path.addFirst(edge);
            node = getNode(node.getParent());
        }
        return path;
    }

    private List<Completion<T>> completionsOfIndex (int index) {
        Node<T> node = getNode(index);
        return node.getCompletions()
                .stream()
                .map(completion -> {
                    int hits = completion.getLeft();
                    int nodeIdx = completion.getRight();
                    List<T> path = pathOf(nodeIdx);
                    return new Completion<>(hits, path);
                })
                .collect(Collectors.toList());
    }

    // TODO: This is wasteful. We can traverse the remaining path for each completion.
    // Returns the completions of a given path as follows: list of (hits of that path, the path)
    public List<Completion<T>> completionsOfPath (List<T> path) {
        synchronized (mutex) {
            Integer index = getFinalNode(path);
            if (index == null) return Collections.emptyList();
            return completionsOfIndex(index);
        }
    }

    private String prefixed (String val) {
        return prefix + ":" + val;
    }

    private String prefixed (Integer val) {
        return prefixed(val.toString());
    }

    // TODO: Reanalyse later if an LinkedList is the correct implementation

    @Value
    public static class Completion<T> {
        int score;
        List<T> path;
    }

    private int incrementOf (InsertType insertType) {
        switch (insertType) {
            case SELECTION: return selectionIncrement;
            case OCCURRENCE: return occurrenceIncrement;
        }
        throw new RuntimeException("Cannot reach here as switch cases are exhaustive");
    }

}
