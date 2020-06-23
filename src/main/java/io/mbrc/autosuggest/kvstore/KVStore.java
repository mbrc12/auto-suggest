package io.mbrc.autosuggest.kvstore;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class KVStore {

    // If we need DEBUG, change this to true, and add
    // .recordStats() to CacheBuilder.newBuilder()

    private static final boolean DEBUG = false;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(KVStore.class);
    private final Gson gson;

    private final Cache<String, String> cache;

    private final PersistenceTask persistenceTask;

    private final Object mutex = new Object();

    @Autowired
    KVStore (Gson gson,
             PersistenceTask persistenceTask,
             KVStoreConfig kvStoreConfig) {
        this.gson = gson;
        this.persistenceTask = persistenceTask;

        this.cache = CacheBuilder.newBuilder()
                .maximumWeight(kvStoreConfig.getMaximumWeight())
                .weigher((String key, String value) -> key.length() + value.length())
                .build();
    }

    public <T> void insert (String key, T value) {
        String repr = gson.toJson(value);
        log.debug("Repr = {}", repr);

        synchronized (mutex) {
            cache.put(key, repr);
            try {
                persistenceTask.insert(key, repr);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Interrupted by user.");
            }
        }
    }

    public <T> void insertAssoc (String key, String assoc, T value) {
        String repr = gson.toJson(value);
        log.debug("Repr = {}", repr);

        synchronized (mutex) {
            cache.put(cacheAssocKey(key, assoc), repr);
            try {
                persistenceTask.insert(persistenceAssocKey(key, assoc), repr);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Interrupted by user.");
            }
        }
    }

    private String query (String key) {
        String repr = cache.getIfPresent(key);

        if (repr == null) {
            synchronized (mutex) {
                repr = cache.getIfPresent(key);
                if (repr == null) {
                    repr = persistenceTask.query(key);
                    if (repr != null)
                        cache.put(key, repr);
                }
            }
        }

        showStats();
        return repr;
    }

    private String queryAssoc (String key, String assoc) {
        final String cacheKey = cacheAssocKey(key, assoc);
        String repr = cache.getIfPresent(cacheKey);

        if (repr == null) {
            synchronized (mutex) {
                repr = cache.getIfPresent(cacheKey);
                if (repr == null) {
                    repr = persistenceTask.query(persistenceAssocKey(key, assoc));
                    if (repr != null) {
                        cache.put(cacheKey, repr);
                    }
                }
            }
        }

        showStats();
        return repr;
    }

    /*
       TODO: Configure these to return Optional<T> instead of (T or null). Node that this will require
        a lot of refactoring.
     */
    public <T> T query (String key, Class<T> clazz) {
        String repr = query(key);
        if (repr == null) return null;
        return gson.fromJson(repr, clazz);
    }

    // TODO: Same as the above query method
    public <T> T query (String key, Type type) {
        String repr = query(key);
        if (repr == null) return null;
        return gson.fromJson(repr, type);
    }

    /*
       TODO: Configure these to return Optional<T> instead of (T or null). Node that this will require
        a lot of refactoring.
     */
    public <T> T queryAssoc (String key, String assoc, Class<T> clazz) {
        String repr = queryAssoc(key, assoc);
        if (repr == null) return null;
        return gson.fromJson(repr, clazz);
    }

    // TODO: Same as the above query method
    public <T> T queryAssoc (String key, String assoc, Type type) {
        String repr = queryAssoc(key, assoc);
        if (repr == null) return null;
        return gson.fromJson(repr, type);
    }

    // ensure that key never has '#'
    private static String persistenceAssocKey (String key, String assoc) {
        assert !key.contains("#");
        return key + "#" + assoc;
    }

    private static String cacheAssocKey (String key, String assoc) {
        assert !key.contains("#");
        return key + "#" + assoc;
    }

    private void showStats() {
        if (DEBUG) {
            CacheStats stats = cache.stats();
            log.info("Stats: {}, {}, {}", stats.hitCount(), stats.missCount(), cache.asMap().size());
        }
    }

    synchronized public void shutdown () throws Exception {
        persistenceTask.destroy();
    }
}
