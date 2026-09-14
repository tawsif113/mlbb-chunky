package com.mlbbchunky.hero.api;

import com.mlbbchunky.hero.application.HeroCatalog;
import com.mlbbchunky.hero.domain.Hero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v1/heroes")
public class HeroController {
    private final HeroCatalog heroCatalog;

    public HeroController(HeroCatalog heroCatalog) {
        this.heroCatalog = heroCatalog;
    }

    @GetMapping
    public List<Hero> list() {
        return heroCatalog.findAll();
    }

    @GetMapping("/{heroId}")
    public Hero get(@PathVariable long heroId) {
        return heroCatalog.findById(heroId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Hero not found"));
    }
}
