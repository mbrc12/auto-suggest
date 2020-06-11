package io.mbrc.autosuggest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RestServer {

    private final IngestTask ingestTask;
    private final CompletionService completionService;

    @Autowired
    RestServer (IngestTask ingestTask,
                CompletionService completionService) {
        this.ingestTask = ingestTask;
        this.completionService = completionService;
    }

    @PostMapping("/index")
    public String index (@RequestBody List<String> titles) {
        titles.forEach(ingestTask::submit);
        return "success";
    }

    @PostMapping("/complete")
    public List<String> complete (@RequestParam String phrase) {
        return completionService.complete(phrase);
    }

    @PostMapping("/select")
    public String select (@RequestParam String selected) {
        ingestTask.selected(selected);
        return "success";
    }
}
