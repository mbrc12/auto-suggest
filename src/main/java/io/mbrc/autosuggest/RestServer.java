package io.mbrc.autosuggest;

import io.mbrc.autosuggest.kvstore.KVStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RestServer {

    private final KVStore kvStore;
    private final IngestTask ingestTask;
    private final CompletionService completionService;

    @Autowired
    RestServer (KVStore kvStore,
                IngestTask ingestTask,
                CompletionService completionService) {
        this.kvStore = kvStore;
        this.ingestTask = ingestTask;
        this.completionService = completionService;
    }

    @PostMapping("/index")
    public String index (List<String> titles) {
        titles.forEach(ingestTask::submit);
        return "success";
    }

    @GetMapping("/complete")
    public List<String> complete (String phrase) {
        return completionService.generateCompletions(phrase);
    }

    @GetMapping("/select")
    public String select (String selected) {
        ingestTask.selected(selected);
        return "success";
    }
}
