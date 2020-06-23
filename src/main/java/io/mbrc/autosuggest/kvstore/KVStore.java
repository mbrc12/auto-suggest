package io.mbrc.autosuggest.kvstore;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class KVStore {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(KVStore.class);
    private final Gson gson;
    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> redisValueOperations;
    private final HashOperations<String, String, String> redisHashOperations;

    private final PersistenceTask persistenceTask;

    private final Object mutex = new Object();

    @Autowired
    KVStore (Gson gson,
             RedisConnectionFactory redisConnectionFactory,
             PersistenceTask persistenceTask) {
        this.gson = gson;

        this.redisTemplate = new RedisTemplate<>();
        this.redisTemplate.setConnectionFactory(redisConnectionFactory);
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setValueSerializer(new StringRedisSerializer());
        this.redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        this.redisTemplate.afterPropertiesSet();


        this.redisValueOperations = this.redisTemplate.opsForValue();
        this.redisHashOperations = this.redisTemplate.opsForHash();

        this.persistenceTask = persistenceTask;
    }

    public <T> void insert (String key, T value) {
        String repr = gson.toJson(value);
        log.debug("Repr = {}", repr);

        synchronized (mutex) {
            redisValueOperations.set(key, repr);
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
            redisHashOperations.put(key, assoc, repr);
            try {
                persistenceTask.insert(persistenceAssocKey(key, assoc), repr);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Interrupted by user.");
            }
        }
    }

    private String query (String key) {
        String repr = redisValueOperations.get(key);

        if (repr == null) {
            synchronized (mutex) {
                repr = redisValueOperations.get(key);
                if (repr == null) {
                    repr = persistenceTask.query(key);
                    if (repr != null)
                        redisValueOperations.set(key, repr);
                }
            }
        }

        return repr;
    }

    private String queryAssoc (String key, String assoc) {
        String repr = redisHashOperations.get(key, assoc);

        if (repr == null) {
            synchronized (mutex) {
                repr = redisHashOperations.get(key, assoc);
                if (repr == null) {
                    repr = persistenceTask.query(persistenceAssocKey(key, assoc));
                    if (repr != null) {
                        redisHashOperations.put(key, assoc, repr);
                    }
                }
            }
        }

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
    private String persistenceAssocKey (String key, String assoc) {
        assert !key.contains("#");
        return key + "#" + assoc;
    }

    synchronized public void shutdown () throws Exception {
        persistenceTask.destroy();
    }
}
