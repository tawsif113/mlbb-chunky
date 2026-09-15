package com.mlbbchunky.hero.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class HeroIngestionService {
    private final Optional<MlbbHeroDataProvider> provider;
    private final HeroIngestionRepository repository;

    public HeroIngestionService(
            Optional<MlbbHeroDataProvider> provider,
            HeroIngestionRepository repository
    ) {
        this.provider = provider;
        this.repository = repository;
    }

    @Transactional
    public SyncResult sync() {
        MlbbHeroDataProvider activeProvider = provider.orElseThrow(() ->
                new IllegalStateException("No MLBB hero data provider is configured")
        );

        List<MlbbHeroDataProvider.HeroData> heroes = activeProvider.fetchHeroes();
        if (heroes.isEmpty()) {
            throw new IllegalStateException("MLBB hero provider returned an empty catalog");
        }

        repository.upsertCatalog(heroes);
        return new SyncResult(heroes.size(), Instant.now());
    }

    public record SyncResult(int importedHeroes, Instant completedAt) {}
}
