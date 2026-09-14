package com.mlbbchunky.draft.api;

import com.mlbbchunky.draft.application.DraftAssistantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/draft")
public class DraftController {
    private final DraftAssistantService draftAssistantService;

    public DraftController(DraftAssistantService draftAssistantService) {
        this.draftAssistantService = draftAssistantService;
    }

    @PostMapping("/recommendations")
    public List<HeroRecommendation> recommend(@Valid @RequestBody DraftRequest request) {
        return draftAssistantService.recommend(request);
    }
}
