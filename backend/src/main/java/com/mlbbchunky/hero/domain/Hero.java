package com.mlbbchunky.hero.domain;

import java.util.Set;

public record Hero(
        long id,
        String name,
        String imageUrl,
        Set<HeroRole> roles,
        Set<Lane> lanes,
        double winRate,
        double pickRate,
        double banRate,
        Set<Long> strongAgainst,
        Set<Long> synergizesWith
) {}
