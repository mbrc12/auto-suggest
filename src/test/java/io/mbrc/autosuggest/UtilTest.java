package io.mbrc.autosuggest;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static io.mbrc.autosuggest.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
@SpringBootTest
public class UtilTest {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UtilTest.class);

    @Test
    public void editDistanceTest () {
        assertEquals(1, editDistance("hello", "hallo"));
    }

    @Test
    public void editDistanceTest2 () {
        assertEquals(1, editDistance("hallo", "hllo"));
    }

    @Test
    public void selectIndicesTest () {
        assertEquals(List.of("will", "work"),
                selectIndices(List.of("This", "will", "probably", "work"), List.of(1, 3)));
    }

    @Test
    public void splitPrefix1 () {
        assertEquals(Optional.of("content"),
                splitPrefix("pr:", prefixed("pr:", "content")));
        assertEquals(Optional.empty(),
                splitPrefix("pr:", prefixed("pg:", "")));
        assertEquals(Optional.of(""),
                splitPrefix("p:", "p:"));
    }

    @Test
    public void chooseTest () {
        assertEquals(choose(5, 2), 10);
        assertEquals(choose(0, 0), 1);
        assertEquals(choose(5, 6), 0);
        assertEquals(choose(6, 3), 20);
        assertEquals(choose(4, 2), 6);
        assertEquals(orderedCombinations(List.of("a", "b", "c", "d"), 2).size(), choose(4, 2));
    }
}
