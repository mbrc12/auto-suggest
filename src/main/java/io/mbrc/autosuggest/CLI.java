package io.mbrc.autosuggest;

import io.mbrc.autosuggest.kvstore.KVStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CLI implements CommandLineRunner{

    private @Autowired KVStore kvStore;
    private @Autowired IngestTask ingestTask;

    @Override
    public void run(String... args) throws Exception {
        log.info("The CLI works apparently.");

////        for (int j = 0; j <= 100; j++) {
////            long ans = 0;
////            for (long i = 0; i <= 100; i++) {
////                ans += kvStore.query(String.format("%d-v", j * 100 + i), Long.class);
////            }
////            log.info("--> {}", ans);
////        }
//

//        List<Integer> list = List.of(1, 2, 3);
//        for (List<Integer> comb : orderedCombinations(list, 2)) {
//            log.info("--> {}", comb.toString());
//        }
//
//        kvStore.shutdown();
//        ingestTask.shutdown();

    }

    @Data
    public static class Greet {
        private String name;
    }
}
