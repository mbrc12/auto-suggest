package io.mbrc.autosuggest.poptrie;

import com.google.gson.annotations.SerializedName;

public class IntPair {
    @SerializedName("l")
    int left;

    @SerializedName("r")
    int right;

    public IntPair(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public static IntPair of (int left, int right) {
        return new IntPair(left, right);
    }

    public static int reverseComparator (IntPair x, IntPair y) {
        int comp = Integer.compare(y.getLeft(), x.getLeft());
        if (comp == 0)
            return Integer.compare(x.getRight(), y.getRight());
        return comp;
    }

    public int getLeft() {
        return this.left;
    }

    public int getRight() {
        return this.right;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof IntPair)) return false;
        final IntPair other = (IntPair) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getLeft() != other.getLeft()) return false;
        if (this.getRight() != other.getRight()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof IntPair;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getLeft();
        result = result * PRIME + this.getRight();
        return result;
    }

    public String toString() {
        return "IntPair(left=" + this.getLeft() + ", right=" + this.getRight() + ")";
    }
}


