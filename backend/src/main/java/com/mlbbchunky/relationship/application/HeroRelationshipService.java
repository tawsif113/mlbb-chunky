package com.mlbbchunky.relationship.application;

import com.mlbbchunky.relationship.application.MlbbRelationshipDataProvider.RelationshipType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Service
public class HeroRelationshipService {
    private static final Set<Integer> SUPPORTED_PERIODS = Set.of(1, 3, 7, 15, 30);
    private static final Set<String> SUPPORTED_RANKS = Set.of("all", "epic", "legend", "mythic", "honor", "glory");

    private final ObjectProvider<MlbbRelationshipDataProvider> providers;
    private final HeroRelationshipRepository repository;

    public HeroRelationshipService(
            ObjectProvider<MlbbRelationshipDataProvider> providers,
            HeroRelationshipRepository repository
    ) {
        this.providers = providers;
        this.repository = repository;
    }

    @Transactional
    public SyncResult sync(String rankScope, int periodDays) {
        String normalizedRank = validateRank(rankScope);
        validatePeriod(periodDays);

        var heroIds = repository.findHeroIds();
        if (heroIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hero catalog is empty; sync heroes first");
        }

        var result = provider().fetchRelationships(heroIds, normalizedRank, periodDays);
        if (result.completedSubjects().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Relationship provider returned no usable hero data");
        }

        int persisted = repository.replaceRelationships(result, normalizedRank, periodDays);
        long completedCounters = result.completedSubjects().stream()
                .filter(item -> item.type() == RelationshipType.COUNTER)
                .count();
        long completedSynergies = result.completedSubjects().stream()
                .filter(item -> item.type() == RelationshipType.SYNERGY)
                .count();

        return new SyncResult(
                normalizedRank,
                periodDays,
                heroIds.size(),
                completedCounters,
                completedSynergies,
                result.relationships().size(),
                persisted,
                Instant.now()
        );
    }

    private MlbbRelationshipDataProvider provider() {
        MlbbRelationshipDataProvider provider = providers.getIfAvailable();
        if (provider == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No MLBB relationship data provider is configured"
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
            int catalogHeroes,
            long completedCounterHeroes,
            long completedSynergyHeroes,
            int fetchedRelationships,
            int persistedRelationships,
            Instant completedAt
    ) {}
}
