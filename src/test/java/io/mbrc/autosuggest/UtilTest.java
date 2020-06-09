package io.mbrc.autosuggest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.mbrc.autosuggest.Util.editDistance;
import static io.mbrc.autosuggest.Util.selectIndices;
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

}
