package io.mbrc.autosuggest.poptrie;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class IntPair {
    int left;
    int right;

    public static IntPair of (int left, int right) {
        return new IntPair(left, right);
    }

    public static int reverseComparator (IntPair x, IntPair y) {
        int comp = Integer.compare(y.getLeft(), x.getLeft());
        if (comp == 0)
            return Integer.compare(x.getRight(), y.getRight());
        return comp;
    }
}


