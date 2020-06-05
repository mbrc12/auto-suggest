package io.mbrc.autosuggest.kvstore;

import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KVStore {

    private final Gson gson;
    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> redisValueOperations;
    private final PersistenceTask persistenceTask;

    @Autowired
    KVStore (Gson gson, RedisConnectionFactory redisConnectionFactory, PersistenceTask persistenceTask) {
        this.gson = gson;

        this.redisTemplate = new RedisTemplate<>();
        this.redisTemplate.setConnectionFactory(redisConnectionFactory);
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setValueSerializer(new StringRedisSerializer());
        this.redisTemplate.afterPropertiesSet();

        this.redisValueOperations = this.redisTemplate.opsForValue();

        this.persistenceTask = persistenceTask;
    }

    public <T> void insert (String key, T value) throws InterruptedException {
        String repr = gson.toJson(value);
        log.debug("Repr = {}", repr);

        redisValueOperations.set(key, repr);
        persistenceTask.insert(key, repr);
    }

    public <T> T query (String key, Class<T> clazz) {
        String repr = redisValueOperations.get(key);
        if (repr == null) {
            repr = persistenceTask.query(key);
            // Since this failed, reinsert into redis
            redisValueOperations.set(key, repr);
        }

        return gson.fromJson(repr, clazz);
    }

    public void shutdown () throws Exception {
        persistenceTask.destroy();
    }

    @Data
    public static class Greet {
        private String name;
    }
}
