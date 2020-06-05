package io.mbrc.autosuggest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Util {

    public static <T> List<List<T>> ordered_combinations_upto (List<T> list, int K) {
        if (K == 0) return Collections.emptyList();

        ArrayList<T> arrayList = new ArrayList<>(list);
        ArrayList<List<T>> combinations = new ArrayList<>();

        for (T item : arrayList) {
            int currentSize = combinations.size();
            combinations.add(List.of(item));
            for (int i = 0; i < currentSize; i++) {
                List<T> comb = combinations.get(i);
                if (comb.size() < K) {
                    List<T> copy = new LinkedList<>(comb);
                    copy.add(item);
                    combinations.add(copy);
                }
            }
        }

        return combinations;
    }

}
