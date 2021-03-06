package io.mbrc.autosuggest;

import com.google.gson.reflect.TypeToken;
import io.mbrc.autosuggest.popmap.Suggestions;
import io.mbrc.autosuggest.poptrie.Node;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class Util {

    public static Type stringSuggestionsType = new TypeToken<Suggestions<String>>(){}.getType();
    public static Type intNodeType = new TypeToken<Node<Integer>>(){}.getType();
    public static Type stringNodeType = new TypeToken<Node<String>>(){}.getType();

    public static <T> List<LinkedList<T>> orderedCombinations(List<T> list, int K) {
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

        return combinations.stream().filter(comb -> comb.size() == K).collect(Collectors.toList());
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

    // Range of integers from [start, end).

    public static List<Integer> rangeList (int start, int end) {
        final List<Integer> list = new LinkedList<>();
        for (int i = start; i < end; i++) {
            list.add(i);
        }
        return list;
    }

    // Returns the elements of list whose indices are in `indices`.
    // If indices is not strictly increasing, the result is undefined
    public static <T> List<T> selectIndices (List<T> list, List<Integer> indices) {
        List<T> result = new LinkedList<>();
        ListIterator<T> iterator = list.listIterator();
        ListIterator<Integer> indexIter = indices.listIterator();

        int currentIndex = 0;

        while (indexIter.hasNext()) {
            int index = indexIter.next();
            while (iterator.hasNext()) {
                T elem = iterator.next();
                if (index == currentIndex) {
                    result.add(elem);
                    currentIndex++;
                    break;
                } else {
                    currentIndex++;
                }
            }
        }

        return result;
    }

    // If string is of the form (prefix)z then return z
    // otherwise return nothing.
    public static Optional<String> splitPrefix (String prefix, String string) {
        if (string.length() < prefix.length())
            return Optional.empty();

        if (!string.startsWith(prefix))
            return Optional.empty();

        return Optional.of(string.substring(prefix.length()));
    }

    public static String prefixed (String prefix, String string) {
        return prefix + string;
    }

    public static boolean isAlphabet (char c) {
        if ('A' <= c && c <= 'Z') return true;
        if ('a' <= c && c <= 'z') return true;
        return false;
    }

    public static boolean isEnglish (int c) {
        return c <= (int)'z';
    }

    public static String stringCleaner (String str) {
        return str; // Do not clean right now
//        return str.replaceAll("\\P{Print}", "");
    }

    public static long choose (int n, int r) {
        if (r < 0) return 0;
        if (r > n) return 0;
        long sol = 1;
        long div = 1;
        for (int i = 0; i < r; i++) {
            sol *= n - i;
            div *= i + 1;
        }
        return sol / div;
    }

    public enum InsertType {
        OCCURRENCE,
        SELECTION
    }
}
