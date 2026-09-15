package com.mlbbchunky.relationship.infrastructure.rone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mlbbchunky.relationship.application.MlbbRelationshipDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "app.mlbb", name = "data-provider", havingValue = "rone")
public class RoneArenaRelationshipDataProvider implements MlbbRelationshipDataProvider {
    private static final Logger log = LoggerFactory.getLogger(RoneArenaRelationshipDataProvider.class);
    private static final int PAGE_SIZE = 50;

    private final RestClient client;

    public RoneArenaRelationshipDataProvider(@Value("${app.mlbb.rone.base-url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public FetchResult fetchRelationships(List<Long> heroIds, String rankScope, int periodDays) {
        Set<Long> knownHeroIds = Set.copyOf(heroIds);
        List<HeroRelationshipData> relationships = new ArrayList<>();
        Set<CompletedSubject> completedSubjects = new HashSet<>();

        for (Long heroId : heroIds) {
            fetchForSubject(heroId, RelationshipType.COUNTER, rankScope, periodDays, knownHeroIds, relationships, completedSubjects);
            fetchForSubject(heroId, RelationshipType.SYNERGY, rankScope, periodDays, knownHeroIds, relationships, completedSubjects);
        }

        return new FetchResult(List.copyOf(relationships), Set.copyOf(completedSubjects));
    }

    private void fetchForSubject(
            long heroId,
            RelationshipType type,
            String rankScope,
            int periodDays,
            Set<Long> knownHeroIds,
            List<HeroRelationshipData> relationships,
            Set<CompletedSubject> completedSubjects
    ) {
        try {
            RelationshipRecord data = fetchPrimary(heroId, type, rankScope, periodDays);
            appendRelationships(heroId, type, data, knownHeroIds, relationships);
            completedSubjects.add(new CompletedSubject(heroId, type));
        } catch (RestClientException | ResponseStatusException primaryFailure) {
            try {
                RelationshipRecord data = fetchAcademyFallback(heroId, type, rankScope);
                appendRelationships(heroId, type, data, knownHeroIds, relationships);
                completedSubjects.add(new CompletedSubject(heroId, type));
                log.debug("Used Rone Academy fallback for hero {} {} relationships", heroId, type);
            } catch (RestClientException | ResponseStatusException fallbackFailure) {
                log.warn(
                        "Skipping {} relationships for hero {}. Primary failed: {}. Academy fallback failed: {}",
                        type,
                        heroId,
                        primaryFailure.getMessage(),
                        fallbackFailure.getMessage()
                );
            }
        }
    }

    private RelationshipRecord fetchPrimary(
            long heroId,
            RelationshipType type,
            String rankScope,
            int periodDays
    ) {
        String path = type == RelationshipType.COUNTER
                ? "/heroes/{heroId}/counters"
                : "/heroes/{heroId}/compatibility";

        RelationshipEnvelope response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("days", periodDays)
                        .queryParam("rank", rankScope)
                        .queryParam("size", PAGE_SIZE)
                        .queryParam("index", 1)
                        .queryParam("lang", "en")
                        .build(heroId))
                .retrieve()
                .body(RelationshipEnvelope.class);

        return extractRecord(response);
    }

    private RelationshipRecord fetchAcademyFallback(
            long heroId,
            RelationshipType type,
            String rankScope
    ) {
        String path = type == RelationshipType.COUNTER
                ? "/academy/heroes/{heroId}/counters"
                : "/academy/heroes/{heroId}/teammates";

        RelationshipEnvelope response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("rank", rankScope)
                        .queryParam("size", PAGE_SIZE)
                        .queryParam("index", 1)
                        .queryParam("lang", "en")
                        .build(heroId))
                .retrieve()
                .body(RelationshipEnvelope.class);

        return extractRecord(response);
    }

    private RelationshipRecord extractRecord(RelationshipEnvelope response) {
        if (response == null || response.code() != 0 || response.data() == null || response.data().records() == null) {
            String message = response == null ? null : firstNonBlank(response.message(), response.msg());
            throw providerFailure(message);
        }

        return response.data().records().stream()
                .map(RelationshipRecordEnvelope::data)
                .filter(data -> data != null)
                .findFirst()
                .orElseGet(() -> new RelationshipRecord(null, List.of()));
    }

    private void appendRelationships(
            long requestedHeroId,
            RelationshipType type,
            RelationshipRecord data,
            Set<Long> knownHeroIds,
            List<HeroRelationshipData> relationships
    ) {
        long subjectHeroId = data.mainHeroId() == null ? requestedHeroId : data.mainHeroId();
        if (!knownHeroIds.contains(subjectHeroId)) {
            return;
        }

        List<RelatedHero> relatedHeroes = data.subHeroes() == null ? List.of() : data.subHeroes();
        for (RelatedHero related : relatedHeroes) {
            if (related == null || related.heroId() == null) continue;
            if (related.heroId() == subjectHeroId || !knownHeroIds.contains(related.heroId())) continue;
            relationships.add(new HeroRelationshipData(
                    subjectHeroId,
                    related.heroId(),
                    type,
                    related.impact()
            ));
        }
    }

    private ResponseStatusException providerFailure(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                message == null || message.isBlank()
                        ? "MLBB relationship data provider request failed"
                        : message
        );
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        return second;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RelationshipEnvelope(int code, String msg, String message, RelationshipData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RelationshipData(List<RelationshipRecordEnvelope> records, Integer total) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RelationshipRecordEnvelope(RelationshipRecord data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RelationshipRecord(
            @JsonProperty("main_heroid") Long mainHeroId,
            @JsonProperty("sub_hero") List<RelatedHero> subHeroes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RelatedHero(
            @JsonProperty("heroid") Long heroId,
            @JsonProperty("increase_win_rate") Double impact
    ) {}
}
