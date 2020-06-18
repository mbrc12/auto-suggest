package io.mbrc.autosuggest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static io.mbrc.autosuggest.Util.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Component
@SpringBootTest
public class UtilTest {

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
    public void combinationsTest () {
        assertEquals(orderedGrams(List.of("a", "b", "c", "d"), 2).size(), computeSize(4, 2));
        assertEquals(computeSize(4, 2), 3);
    }
}
