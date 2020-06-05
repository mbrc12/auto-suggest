package io.mbrc.autosuggest.kvstore;

import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "kvstore")
public class KVStoreConfig {

    public @Bean
    Gson gson () {
        return new Gson();
    }

    private String persistDb;
    private String persistCollection;

    private String persistPrefix;
    private String persistFinishSymbol;
    private Integer commitBatchSize;
}
