package com.mlbbchunky.hero.application;

import java.util.List;

import com.mlbbchunky.hero.application.MlbbHeroDataProvider.HeroData;

public interface HeroIngestionRepository {
    void upsertCatalog(List<HeroData> heroes);
}
