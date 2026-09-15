package com.mlbbchunky.meta.infrastructure.rone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mlbbchunky.meta.application.MlbbMetaDataProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.mlbb", name = "data-provider", havingValue = "rone")
public class RoneArenaMetaDataProvider implements MlbbMetaDataProvider {
    private static final int PAGE_SIZE = 200;

    private final RestClient client;

    public RoneArenaMetaDataProvider(@Value("${app.mlbb.rone.base-url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public List<HeroMetaData> fetchHeroMeta(String rankScope, int periodDays) {
        HeroRankEnvelope response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/heroes/heroes/rank")
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
}
