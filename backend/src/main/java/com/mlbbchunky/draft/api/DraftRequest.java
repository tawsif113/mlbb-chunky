package com.mlbbchunky.draft.api;

import com.mlbbchunky.hero.domain.HeroRole;
import com.mlbbchunky.hero.domain.Lane;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record DraftRequest(
        @NotNull Lane lane,
        HeroRole preferredRole,
        Set<Long> alliedHeroIds,
        Set<Long> enemyHeroIds,
        Set<Long> bannedHeroIds
) {
    public DraftRequest {
        alliedHeroIds = alliedHeroIds == null ? Set.of() : Set.copyOf(alliedHeroIds);
        enemyHeroIds = enemyHeroIds == null ? Set.of() : Set.copyOf(enemyHeroIds);
        bannedHeroIds = bannedHeroIds == null ? Set.of() : Set.copyOf(bannedHeroIds);
    }
}
