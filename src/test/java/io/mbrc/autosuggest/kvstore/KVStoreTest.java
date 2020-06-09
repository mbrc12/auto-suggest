package io.mbrc.autosuggest.kvstore;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.mbrc.autosuggest.Util.rangeList;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Component
@SpringBootTest
public class KVStoreTest {

    private @Autowired KVStore kvStore;

    @Test
    public void singleThread1 () {
        assertNotNull(kvStore);
        kvStore.insert("test:a", "Wow");
        assertEquals(kvStore.query("test:a", String.class), "Wow");
    }

    // This next test takes up a lot of time (~10s on my machine).
    // Uncomment the @Test annotation if you want to check this.
//    @Test
    public void multiThread1 () throws InterruptedException {
        int N = 100000;
        String prefix = "multiThread1-1:";

        ExecutorService pool = Executors.newFixedThreadPool(10);

        final CountDownLatch[] latches = new CountDownLatch[N];
        for (int i = 0; i < N; i++)
            latches[i] = new CountDownLatch(1);

        for (int i = 0; i < N; i++) {
            final int x = i;
            pool.submit(() -> {
                kvStore.insert(prefix + x, x);
                latches[x].countDown();
            });
        }

        final LinkedList<Integer> done = new LinkedList<>();

        for (int i = 0; i < N; i++) {
            final int x = i;
            pool.submit(() -> {
                assertDoesNotThrow(() -> latches[x].await());
                Integer val = kvStore.query(prefix + x, Integer.class);
                assertNotNull(val);
                assertEquals(val, x);
                synchronized (done) {
                    done.add(x);
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        done.sort(Integer::compare);

        //  Check if all the processes finished successfully.
        assertEquals(done, rangeList(0, N));
    }
}
