package com.mlbbchunky.hero.infrastructure.rone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mlbbchunky.hero.application.MlbbHeroDataProvider;
import com.mlbbchunky.hero.domain.HeroRole;
import com.mlbbchunky.hero.domain.Lane;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "app.mlbb", name = "data-provider", havingValue = "rone")
public class RoneArenaHeroDataProvider implements MlbbHeroDataProvider {
    private static final int PAGE_SIZE = 200;

    private final RestClient client;

    public RoneArenaHeroDataProvider(
            RestClient.Builder builder,
            @Value("${app.mlbb.rone.base-url}") String baseUrl
    ) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    @Override
    public List<HeroData> fetchHeroes() {
        Map<Long, HeroAccumulator> heroes = new LinkedHashMap<>();

        merge(heroes, fetchPage(null, null), null, null);

        for (HeroRole role : HeroRole.values()) {
            merge(heroes, fetchPage("role", role.name().toLowerCase(Locale.ROOT)), role, null);
        }
        for (Lane lane : Lane.values()) {
            merge(heroes, fetchPage("lane", lane.name().toLowerCase(Locale.ROOT)), null, lane);
        }

        return heroes.values().stream()
                .map(HeroAccumulator::toHeroData)
                .sorted(Comparator.comparingLong(HeroData::id))
                .toList();
    }

    private List<HeroRecordEnvelope> fetchPage(String filterName, String filterValue) {
        HeroListEnvelope response = client.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/academy/heroes")
                            .queryParam("size", PAGE_SIZE)
                            .queryParam("index", 1)
                            .queryParam("order", "asc")
                            .queryParam("lang", "en");
                    if (filterName != null && filterValue != null) {
                        builder.queryParam(filterName, filterValue);
                    }
                    return builder.build();
                })
                .retrieve()
                .body(HeroListEnvelope.class);

        if (response == null || response.code() != 0 || response.data() == null) {
            String message = response == null ? null : firstNonBlank(response.message(), response.msg());
            throw providerFailure(message);
        }

        return response.data().records() == null ? List.of() : response.data().records();
    }

    private void merge(
            Map<Long, HeroAccumulator> heroes,
            List<HeroRecordEnvelope> records,
            HeroRole role,
            Lane lane
    ) {
        for (HeroRecordEnvelope recordEnvelope : records) {
            HeroRecord record = recordEnvelope == null ? null : recordEnvelope.data();
            if (record == null || record.heroId() == null) {
                continue;
            }

            String name = record.hero() == null || record.hero().data() == null
                    ? null
                    : record.hero().data().name();
            if (name == null || name.isBlank()) {
                continue;
            }

            HeroAccumulator hero = heroes.computeIfAbsent(
                    record.heroId(),
                    ignored -> new HeroAccumulator(record.heroId(), name, record.head())
            );
            hero.refreshIdentity(name, record.head());
            if (role != null) hero.roles.add(role);
            if (lane != null) hero.lanes.add(lane);
        }
    }

    private ResponseStatusException providerFailure(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                message == null || message.isBlank()
                        ? "MLBB hero data provider request failed"
                        : message
        );
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        return second;
    }

    private static final class HeroAccumulator {
        private final long id;
        private String name;
        private String imageUrl;
        private final EnumSet<HeroRole> roles = EnumSet.noneOf(HeroRole.class);
        private final EnumSet<Lane> lanes = EnumSet.noneOf(Lane.class);

        private HeroAccumulator(long id, String name, String imageUrl) {
            this.id = id;
            this.name = name;
            this.imageUrl = imageUrl;
        }

        private void refreshIdentity(String incomingName, String incomingImageUrl) {
            if (incomingName != null && !incomingName.isBlank()) this.name = incomingName;
            if (incomingImageUrl != null && !incomingImageUrl.isBlank()) this.imageUrl = incomingImageUrl;
        }

        private HeroData toHeroData() {
            return new HeroData(id, name, imageUrl, Set.copyOf(roles), Set.copyOf(lanes));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroListEnvelope(int code, String msg, String message, HeroListData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroListData(List<HeroRecordEnvelope> records, Integer total) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroRecordEnvelope(HeroRecord data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroRecord(
            @JsonProperty("hero_id") Long heroId,
            String head,
            HeroContainer hero
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroContainer(HeroNameData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HeroNameData(String name) {}
}
