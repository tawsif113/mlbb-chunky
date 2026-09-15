package com.mlbbchunky.meta.application;

import java.util.List;

public interface MlbbMetaDataProvider {

    List<HeroMetaData> fetchHeroMeta(String rankScope, int periodDays);

    record HeroMetaData(
            long heroId,
            double pickRate,
            double banRate,
            double winRate
    ) {}
}
