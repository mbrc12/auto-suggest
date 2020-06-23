package io.mbrc.autosuggest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.mbrc.autosuggest.Util.stringCleaner;

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

    // Clean all input before sending using stringCleaner

    @PostMapping("/index")
    public String index (@RequestBody List<String> titles) {
        titles.forEach(title ->
                ingestTask.submit(stringCleaner(title)));
        return "success";
    }

    @PostMapping("/complete")
    public List<String> complete (@RequestParam String phrase) {
        return completionService.complete(stringCleaner(phrase));
    }

    @PostMapping("/select")
    public String select (@RequestParam String selected) {
        ingestTask.selected(stringCleaner(selected));
        return "success";
    }
}
