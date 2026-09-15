package com.mlbbchunky.meta.application;

import com.mlbbchunky.meta.application.HeroMetaRepository.HeroMetaSnapshot;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class HeroMetaService {
    private static final Set<Integer> SUPPORTED_PERIODS = Set.of(1, 3, 7, 15, 30);
    private static final Set<String> SUPPORTED_RANKS = Set.of("all", "epic", "legend", "mythic", "honor", "glory");

    private final ObjectProvider<MlbbMetaDataProvider> providers;
    private final HeroMetaRepository repository;

    public HeroMetaService(
            ObjectProvider<MlbbMetaDataProvider> providers,
            HeroMetaRepository repository
    ) {
        this.providers = providers;
        this.repository = repository;
    }

    @Transactional
    public SyncResult sync(String rankScope, int periodDays) {
        String normalizedRank = validateRank(rankScope);
        validatePeriod(periodDays);
        var snapshots = provider().fetchHeroMeta(normalizedRank, periodDays);
        Instant capturedAt = Instant.now();
        int persisted = repository.appendSnapshots(snapshots, normalizedRank, periodDays, capturedAt);
        return new SyncResult(normalizedRank, periodDays, snapshots.size(), persisted, capturedAt);
    }

    public List<HeroMetaSnapshot> latest(String rankScope, int periodDays) {
        String normalizedRank = validateRank(rankScope);
        validatePeriod(periodDays);
        return repository.findLatest(normalizedRank, periodDays);
    }

    private MlbbMetaDataProvider provider() {
        MlbbMetaDataProvider provider = providers.getIfAvailable();
        if (provider == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No MLBB meta data provider is configured"
            );
        }
        return provider;
    }

    private String validateRank(String rankScope) {
        String normalized = rankScope == null ? "all" : rankScope.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_RANKS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported rank scope: " + rankScope);
        }
        return normalized;
    }

    private void validatePeriod(int periodDays) {
        if (!SUPPORTED_PERIODS.contains(periodDays)) {
            throw new IllegalArgumentException("periodDays must be one of 1, 3, 7, 15, 30");
        }
    }

    public record SyncResult(
            String rankScope,
            int periodDays,
            int fetched,
            int persisted,
            Instant capturedAt
    ) {}
}
