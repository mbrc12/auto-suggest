package io.mbrc.autosuggest.poptrie;

import java.util.LinkedList;
import java.util.List;

public class PopularityTrieHelper {

    public static List<Integer> asIntegerList (String string) {
        final List<Integer> list = new LinkedList<>();
        string.codePoints().forEach(list::add);
        return list;
    }

    public static String asString (List<Integer> list) {
        final StringBuilder builder = new StringBuilder();
        list.forEach(codePoint -> builder.append(Character.toString(codePoint)));
        return builder.toString();
    }
}
