package io.mbrc.autosuggest.poptrie;

import io.mbrc.autosuggest.kvstore.KVStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static io.mbrc.autosuggest.poptrie.PopularityTrieHelper.*;
import static io.mbrc.autosuggest.poptrie.PopularityTrie.*;
import static io.mbrc.autosuggest.Util.*;

@Component
@SpringBootTest
public class PopTrieTest {

    private @Autowired
    KVStore kvStore;

    @Test
    public void inverseHelpers () {
        String test = "\uD83D\uDE02华语胡西它尔ひらがなabcdʊmlaʊtallécher";
        assertEquals(asString(asIntegerList(test)), test);
    }

    @Test
    public void popTrie1 () {
        PopularityTrie<String> popularityTrie = PopularityTrie.getInstance
                (kvStore, "pop-2", 1, 2, 4,
                        stringNodeType);
        assertNotNull(popularityTrie);
        popularityTrie.insert(List.of("covid", "india", "donald"), InsertType.OCCURRENCE);
        List<Completion<String>> ans = popularityTrie.completionsOfPath(List.of("covid"));
        assertTrue(ans.size() >= 1);
    }

    @Test
    public void popTrie2 () {
        PopularityTrie<String> popularityTrie = PopularityTrie.getInstance
                (kvStore, "pop-3", 1, 2, 4,
                        stringNodeType);
        assertNotNull(popularityTrie);
        popularityTrie.insert(List.of("covid", "india", "donald"), InsertType.OCCURRENCE);
        popularityTrie.insert(List.of("covid", "india", "modi"), InsertType.OCCURRENCE);
        popularityTrie.insert(List.of("covid", "india", "modi"), InsertType.SELECTION);
        List<Completion<String>> ans = popularityTrie.completionsOfPath(List.of("covid"));
        assertTrue(ans.size() >= 2);
        assertEquals(3, ans.get(0).getScore() / ans.get(1).getScore()); // ratio should be 3 always
    }
}
