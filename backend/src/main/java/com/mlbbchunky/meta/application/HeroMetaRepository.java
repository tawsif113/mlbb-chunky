package com.mlbbchunky.meta.application;

import com.mlbbchunky.meta.application.MlbbMetaDataProvider.HeroMetaData;

import java.time.Instant;
import java.util.List;

public interface HeroMetaRepository {

    int appendSnapshots(List<HeroMetaData> snapshots, String rankScope, int periodDays, Instant capturedAt);

    List<HeroMetaSnapshot> findLatest(String rankScope, int periodDays);

    record HeroMetaSnapshot(
            long heroId,
            String heroName,
            String imageUrl,
            String rankScope,
            int periodDays,
            double pickRate,
            double banRate,
            double winRate,
            Instant capturedAt
    ) {}
}
