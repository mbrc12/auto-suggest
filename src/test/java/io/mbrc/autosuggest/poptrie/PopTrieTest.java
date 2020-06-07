package io.mbrc.autosuggest.poptrie;

import io.mbrc.autosuggest.kvstore.KVStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static io.mbrc.autosuggest.poptrie.PopularityTrieHelper.*;

@Component
@SpringBootTest
public class PopTrieTest {

    private @Autowired
    KVStore kvStore;

    private @Autowired String appName;

    @Test
    public void helperTestForInverses1() {
        String original = "Hello World!";
        assertEquals(original, asString(asCharacterList(original)));
    }

    @Test
    public void helperTestForInverses2() {
        List<Character> original = List.of('H', 'e', 'l', 'l', 'o', '!');
        assertEquals(original.toString(), asCharacterList(asString(original)).toString());
    }
}
