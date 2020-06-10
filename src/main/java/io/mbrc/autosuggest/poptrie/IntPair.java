package io.mbrc.autosuggest.poptrie;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class IntPair {
    @SerializedName("l")
    int left;

    @SerializedName("r")
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


