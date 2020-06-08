package io.mbrc.autosuggest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Util {

    public static <T> List<LinkedList<T>> orderedCombinationsUpto(List<T> list, int K) {
        if (K == 0) return Collections.emptyList();

        ArrayList<T> arrayList = new ArrayList<>(list);
        ArrayList<LinkedList<T>> combinations = new ArrayList<>();

        for (T item : arrayList) {
            int currentSize = combinations.size();
            combinations.add(new LinkedList<>(List.of(item)));
            for (int i = 0; i < currentSize; i++) {
                List<T> comb = combinations.get(i);
                if (comb.size() < K) {
                    LinkedList<T> copy = new LinkedList<>(comb);
                    copy.add(item);
                    combinations.add(copy);
                }
            }
        }

        return combinations;
    }

    // Shamelessly copied from https://www.baeldung.com/java-levenshtein-distance

    static int editDistance (String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min3(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static int min3(int x, int y, int z) {
        if (x <= y) {
            return Math.min(x, z);
        } else {
            return Math.min(y, z);
        }
    }

    public enum InsertType {
        OCCURRENCE,
        SELECTION
    }
}
