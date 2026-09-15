package com.mlbbchunky.hero.infrastructure;

import com.mlbbchunky.hero.application.HeroCatalog;
import com.mlbbchunky.hero.domain.Hero;
import com.mlbbchunky.hero.domain.HeroRole;
import com.mlbbchunky.hero.domain.Lane;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class SeedHeroCatalog implements HeroCatalog {
    // Temporary fallback data. DatabaseHeroCatalog takes over after live ingestion.
    private final List<Hero> heroes = List.of(
            new Hero(1, "Miya", null, Set.of(HeroRole.MARKSMAN), Set.of(Lane.GOLD), 0.50, 0.07, 0.01, Set.of(), Set.of(6L)),
            new Hero(6, "Tigreal", null, Set.of(HeroRole.TANK), Set.of(Lane.ROAM), 0.51, 0.05, 0.03, Set.of(), Set.of(1L)),
            new Hero(20, "Lolita", null, Set.of(HeroRole.SUPPORT, HeroRole.TANK), Set.of(Lane.ROAM), 0.53, 0.04, 0.08, Set.of(1L), Set.of()),
            new Hero(36, "Aurora", null, Set.of(HeroRole.MAGE), Set.of(Lane.MID), 0.52, 0.05, 0.04, Set.of(), Set.of(6L)),
            new Hero(65, "Claude", null, Set.of(HeroRole.MARKSMAN), Set.of(Lane.GOLD), 0.51, 0.06, 0.05, Set.of(), Set.of(6L)),
            new Hero(84, "Ling", null, Set.of(HeroRole.ASSASSIN), Set.of(Lane.JUNGLE), 0.50, 0.05, 0.10, Set.of(), Set.of())
    );

    @Override
    public List<Hero> findAll() {
        return heroes;
    }

    @Override
    public Optional<Hero> findById(long id) {
        return heroes.stream().filter(hero -> hero.id() == id).findFirst();
    }
}
