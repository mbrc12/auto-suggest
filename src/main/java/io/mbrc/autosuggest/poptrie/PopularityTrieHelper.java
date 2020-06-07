package io.mbrc.autosuggest.poptrie;

import java.util.LinkedList;
import java.util.List;

public class PopularityTrieHelper {

    public static List<Character> asCharacterList (String string) {
        List<Character> list = new LinkedList<>();
        for (int i = 0; i < string.length(); i++) {
            list.add(string.charAt(i));
        }
        return list;
    }

    public static String asString (List<Character> list) {
        final StringBuilder builder = new StringBuilder();
        list.forEach(builder::append);
        return builder.toString();
    }
}
