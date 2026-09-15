package com.mlbbchunky.hero.application;

import com.mlbbchunky.hero.domain.HeroRole;
import com.mlbbchunky.hero.domain.Lane;

import java.util.List;
import java.util.Set;

public interface MlbbHeroDataProvider {
    List<HeroData> fetchHeroes();

    record HeroData(
            long id,
            String name,
            String imageUrl,
            Set<HeroRole> roles,
            Set<Lane> lanes
    ) {}
}
