package com.mlbbchunky.meta.infrastructure.rone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mlbbchunky.meta.application.MlbbMetaDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "app.mlbb", name = "data-provider", havingValue = "rone")
public class RoneArenaMetaDataProvider implements MlbbMetaDataProvider {
    private static final Logger log = LoggerFactory.getLogger(RoneArenaMetaDataProvider.class);
    private static final int PAGE_SIZE = 200;
    private static final Set<Integer> TREND_PERIODS = Set.of(7, 15, 30);

    private final RestClient client;

    public RoneArenaMetaDataProvider(@Value("${app.mlbb.rone.base-url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public List<HeroMetaData> fetchHeroMeta(String rankScope, int periodDays) {
        try {
            return fetchBulkHeroRank(rankScope, periodDays);
        } catch (RestClientResponseException | ResponseStatusException bulkFailure) {
            if (!TREND_PERIODS.contains(periodDays)) {
                throw bulkFailure;
            }

            log.warn(
                    "Rone bulk hero-rank endpoint failed ({}). Falling back to per-hero trend endpoints for {}d / {}.",
                    bulkFailure.getMessage(),
                    periodDays,
                    rankScope
            );
            return fetchTrendFallback(rankScope, periodDays);
        }
    }

    private List<HeroMetaData> fetchBulkHeroRank(String rankScope, int periodDays) {
        HeroRankEnvelope response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/heroes/rank")
                        .queryParam("days", periodDays)
                        .queryParam("rank", rankScope)
                        .queryParam("sort_field", "pick_rate")
                        .queryParam("sort_order", "desc")
                        .queryParam("size", PAGE_SIZE)
                        .queryParam("index", 1)
                        .queryParam("lang", "en")
                        .build())
                .retrieve()
                .body(HeroRankEnvelope.class);

        if (response == null || response.code() != 0 || response.data() == null) {
            String message = response == null ? null : firstNonBlank(response.message(), response.msg());
            throw providerFailure(message);
        }

        List<HeroRankRecordEnvelope> records = response.data().records();
        if (records == null) {
            return List.of();
        }

        return records.stream()
                .map(HeroRankRecordEnvelope::data)
                .filter(data -> data != null
                        && data.heroId() != null
                        && data.pickRate() != null
                        && data.banRate() != null
                        && data.winRate() != null)
                .map(data -> new HeroMetaData(
                        data.heroId(),
                        data.pickRate(),
                        data.banRate(),
                        data.winRate()
                ))
                .toList();
    }

    private List<HeroMetaData> fetchTrendFallback(String rankScope, int periodDays) {
        List<Long> heroIds = fetchHeroIds();
        List<HeroMetaData> result = new ArrayList<>();

        for (Long heroId : heroIds) {
            try {
                HeroMetaData meta = fetchHeroTrend(heroId, rankScope, periodDays);
                if (meta != null) {
                    result.add(meta);
                }
            } catch (RestClientResponseException | ResponseStatusException heroFailure) {
                log.warn("Skipping Rone trend fallback for hero {}: {}", heroId, heroFailure.getMessage());
            }
        }

        if (result.isEmpty()) {
            throw providerFailure("Rone bulk meta and per-hero trend fallbacks returned no usable hero statistics");
        }

        log.info(
                "Rone trend fallback produced {} hero meta records from {} catalog heroes",
                result.size(),
                heroIds.size()
        );
        return result;
    }

    private List<Long> fetchHeroIds() {
        AcademyHeroListEnvelope response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/academy/heroes")
                        .queryParam("size", PAGE_SIZE)
                        .queryParam("index", 1)
                        .queryParam("order", "asc")
                        .queryParam("lang", "en")
                        .build())
                .retrieve()
                .body(AcademyHeroListEnvelope.class);

        if (response == null || response.code() != 0 || response.data() == null || response.data().records() == null) {
            String message = response == null ? null : firstNonBlank(response.message(), response.msg());
            throw providerFailure(message);
        }

        return response.data().records().stream()
                .map(AcademyHeroRecordEnvelope::data)
                .filter(data -> data != null && data.heroId() != null)
                .map(AcademyHeroRecord::heroId)
                .distinct()
                .toList();
    }

    private HeroMetaData fetchHeroTrend(long heroId, String rankScope, int periodDays) {
        try {
            return fetchHeroesTrend(heroId, rankScope, periodDays);
        } catch (RestClientResponseException | ResponseStatusException heroesFailure) {
            log.debug(
                    "Rone /heroes/{}/trends failed ({}); trying Academy trend endpoint.",
                    heroId,
                    heroesFailure.getMessage()
            );
            return fetchAcademyTrend(heroId, rankScope, periodDays);
        }
    }

    private HeroMetaData fetchHeroesTrend(long heroId, String rankScope, int periodDays) {
        TrendEnvelope response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/heroes/{heroId}/trends")
                        .queryParam("rank", rankScope)
                        .queryParam("past-days", periodDays)
                        .queryParam("size", 20)
                        .queryParam("index", 1)
                        .queryParam("lang", "en")
                        .build(heroId))
                .retrieve()
                .body(TrendEnvelope.class);

        return aggregateTrendResponse(response, heroId);
    }

    private HeroMetaData fetchAcademyTrend(long heroId, String rankScope, int periodDays) {
        TrendEnvelope response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/academy/heroes/{heroId}/trends")
                        .queryParam("days", periodDays)
                        .queryParam("rank", rankScope)
                        .queryParam("size", 20)
                        .queryParam("index", 1)
                        .queryParam("lang", "en")
                        .build(heroId))
                .retrieve()
                .body(TrendEnvelope.class);

        return aggregateTrendResponse(response, heroId);
    }

    private HeroMetaData aggregateTrendResponse(TrendEnvelope response, long heroId) {
        if (response == null || response.code() != 0 || response.data() == null || response.data().records() == null) {
            String message = response == null ? null : firstNonBlank(response.message(), response.msg());
            throw providerFailure(message);
        }

        List<DailyRate> dailyRates = response.data().records().stream()
                .map(TrendRecordEnvelope::data)
                .filter(data -> data != null && data.rates() != null)
                .flatMap(data -> data.rates().stream())
                .filter(rate -> rate != null
                        && rate.pickRate() != null
                        && rate.banRate() != null
                        && rate.winRate() != null)
                .toList();

        if (dailyRates.isEmpty()) {
            return null;
        }

        double pickRate = dailyRates.stream().mapToDouble(DailyRate::pickRate).average().orElse(0.0);
        double banRate = dailyRates.stream().mapToDouble(DailyRate::banRate).average().orElse(0.0);
        double winRate = dailyRates.stream().mapToDouble(DailyRate::winRate).average().orElse(0.0);

        return new HeroMetaData(heroId, pickRate, banRate, winRate);
    }

    private ResponseStatusException providerFailure(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                message == null || message.isBlank()
                        ? "MLBB meta data provider request failed"
                        : message
        );
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        return second;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroRankEnvelope(int code, String msg, String message, HeroRankData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroRankData(List<HeroRankRecordEnvelope> records, Integer total) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroRankRecordEnvelope(HeroRankRecord data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroRankRecord(
            @JsonProperty("main_heroid") Long heroId,
            @JsonProperty("main_hero_appearance_rate") Double pickRate,
            @JsonProperty("main_hero_ban_rate") Double banRate,
            @JsonProperty("main_hero_win_rate") Double winRate
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AcademyHeroListEnvelope(int code, String msg, String message, AcademyHeroListData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AcademyHeroListData(List<AcademyHeroRecordEnvelope> records, Integer total) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AcademyHeroRecordEnvelope(AcademyHeroRecord data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AcademyHeroRecord(@JsonProperty("hero_id") Long heroId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TrendEnvelope(int code, String msg, String message, TrendData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TrendData(List<TrendRecordEnvelope> records, Integer total) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TrendRecordEnvelope(TrendRecord data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TrendRecord(
            @JsonProperty("main_heroid") Long heroId,
            @JsonProperty("win_rate") List<DailyRate> rates
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DailyRate(
            @JsonProperty("app_rate") Double pickRate,
            @JsonProperty("ban_rate") Double banRate,
            @JsonProperty("win_rate") Double winRate
    ) {}
}
