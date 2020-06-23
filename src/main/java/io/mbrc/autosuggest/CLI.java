package io.mbrc.autosuggest;

import io.mbrc.autosuggest.kvstore.KVStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CLI implements CommandLineRunner{

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CLI.class);
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

    public static class Greet {
        private String name;

        public Greet() {
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof Greet)) return false;
            final Greet other = (Greet) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$name = this.getName();
            final Object other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof Greet;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            return result;
        }

        public String toString() {
            return "CLI.Greet(name=" + this.getName() + ")";
        }
    }
}
