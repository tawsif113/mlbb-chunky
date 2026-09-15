package com.mlbbchunky.meta.api;

import com.mlbbchunky.meta.application.HeroMetaRepository.HeroMetaSnapshot;
import com.mlbbchunky.meta.application.HeroMetaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meta/heroes")
public class MetaController {
    private final HeroMetaService service;

    public MetaController(HeroMetaService service) {
        this.service = service;
    }

    @GetMapping
    public List<HeroMetaSnapshot> latest(
            @RequestParam(defaultValue = "all") String rankScope,
            @RequestParam(defaultValue = "7") int periodDays
    ) {
        return service.latest(rankScope, periodDays);
    }
}
