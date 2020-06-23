package io.mbrc.autosuggest.kvstore;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kvstore")
public class KVStoreConfig {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(KVStoreConfig.class);

    public @Bean
    Gson gson () {
        return new Gson();
    }

    private String persistDb;
    private String persistCollection;

    private String persistPrefix;
    private String persistFinishSymbol;
    private Integer commitBatchSize;

    public Long maximumWeight;

    public String getPersistDb() {
        return this.persistDb;
    }

    public String getPersistCollection() {
        return this.persistCollection;
    }

    public String getPersistPrefix() {
        return this.persistPrefix;
    }

    public String getPersistFinishSymbol() {
        return this.persistFinishSymbol;
    }

    public Integer getCommitBatchSize() {
        return this.commitBatchSize;
    }

    public void setPersistDb(String persistDb) {
        this.persistDb = persistDb;
    }

    public void setPersistCollection(String persistCollection) {
        this.persistCollection = persistCollection;
    }

    public void setPersistPrefix(String persistPrefix) {
        this.persistPrefix = persistPrefix;
    }

    public void setPersistFinishSymbol(String persistFinishSymbol) {
        this.persistFinishSymbol = persistFinishSymbol;
    }

    public void setCommitBatchSize(Integer commitBatchSize) {
        this.commitBatchSize = commitBatchSize;
    }

    public Long getMaximumWeight() {
        return maximumWeight;
    }

    public void setMaximumWeight(Long maximumWeight) {
        this.maximumWeight = maximumWeight;
    }

}
