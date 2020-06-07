package io.mbrc.autosuggest.kvstore;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PersistenceTask implements DisposableBean {

    private final MongoClient mongoClient;
    private final String persistDb;
    private final String persistCollection;
    private final MongoCollection<Document> mongoCollection;

    private final String persistPrefix;
    private final String persistFinishSymbol;
    private final Integer commitBatchSize;

    private final AtomicBoolean finishCalled;
    private final BlockingDeque<Pair<String, String>> queue;
    private final CountDownLatch finishLatch;

    private final ConcurrentHashMap<String, String> temporaryCache;

    @Autowired
    PersistenceTask (MongoClient mongoClient,
                     KVStoreConfig config) {


        log.info("DB = {}", config.getPersistDb());
        log.info("Coll = {}", config.getPersistCollection());

        this.mongoClient = mongoClient;
        this.persistDb = config.getPersistDb();
        this.persistCollection = config.getPersistCollection();
        this.mongoCollection = mongoClient.getDatabase(this.persistDb).getCollection(this.persistCollection);

        this.persistPrefix = config.getPersistPrefix();
        this.persistFinishSymbol = config.getPersistFinishSymbol();
        this.commitBatchSize = config.getCommitBatchSize();

        this.finishCalled = new AtomicBoolean(false);
        this.queue = new LinkedBlockingDeque<>();
        this.finishLatch = new CountDownLatch(1);

        this.temporaryCache = new ConcurrentHashMap<>();

        final Thread workerThread = new Thread(this::worker);
        workerThread.start();
    }

    // Should be called exactly once because PersistenceTask is singleton
    // and this is only called in the constructor.

    private void worker () {
        boolean finishNow = false;
        final List<Pair<String, String>> currentItems = new LinkedList<>();

        while (!finishNow) {

            int left = commitBatchSize;
            currentItems.clear();

            while (left > 0) {
                try {
                    Pair<String, String> item = queue.takeFirst();
                    if (item.getFirst().equals(persistFinishSymbol)) {
                        log.info("Found finish symbol. Finishing...");
                        finishNow = true;
                        break;
                    } else {
                        left--;
                        currentItems.add(item);
                    }
                } catch (InterruptedException e) {
                    log.error("Interrupted worker thread.");
                    e.printStackTrace();
                    return;
                }
            }

            boolean success = insertBatchToMongo(currentItems);
            if (success) {
                temporaryCache.clear(); // Clear the temporary cache
            } else {
                throw new IllegalStateException("Could not write to mongo.");
            }
        }

        // Release threads waiting on this to finish
        finishLatch.countDown();
    }

    // Only one thread accesses this and that is the workerThread
    // TODO: Later implement an Exponential Backoff
    private boolean insertBatchToMongo (List<Pair<String, String>> batch) {
        if (batch.isEmpty()) return true;
        try {
            mongoCollection.bulkWrite(
                    batch.stream()
                    .map(item -> {
                        String key = item.getFirst(), value = item.getSecond();
                        Bson filter = Filters.eq("_id", key);
                        Bson update = Updates.set("value", value);
                        UpdateOptions options = new UpdateOptions().upsert(true);
                        return new UpdateOneModel<Document>(filter, update, options);
                    })
                    .collect(Collectors.toList()),
                    new BulkWriteOptions().ordered(true) // need ordered, because
            );
            log.info("Persisted a batch of objects");
            return true;
        } catch (MongoException e) {
            log.error("Failed to write to mongo");
            return false;
        }
    }

    // Multiple threads can access this to register an object to be inserted
    // or overwritten. This method should run really fast, and possibility with
    // zero blocking. The underlying implementation of the queue is LinkedBlockingDeque
    // which shouldn't block on a `putLast`

    public void insert (String key, String value) throws InterruptedException {
        temporaryCache.put(key, value);
        queue.putLast(Pair.of(prefixed(key), value));
    }

    public String query (String key) throws MongoException {
        if (temporaryCache.containsKey(key))
            return temporaryCache.get(key);

        // Otherwise query mongo
        log.debug("Querying mongo... {}", key);

        Bson filter = Filters.eq("_id", prefixed(key));

        synchronized (mongoCollection) {
            for (Document doc : mongoCollection.find(filter)) {
                return (String) doc.get("value"); // even if there is one, return
            }
        }

        return null;
    }

    private String prefixed (String key) {
        return persistPrefix + ":" + key;
    }

    public void finishPersistence () throws InterruptedException {
        boolean previous = finishCalled.getAndSet(true);
        if (previous) return;

        queue.putLast(Pair.of(persistFinishSymbol, "dummy-value"));

        // Wait for the worker thread to shutdown.
        finishLatch.await();
    }

    @Override
    public void destroy () throws Exception {
        finishPersistence();
        mongoClient.close();
    }
}
