package com.mlbbchunky.draft.application;

import com.mlbbchunky.draft.api.DraftRequest;
import com.mlbbchunky.hero.application.HeroCatalog;
import com.mlbbchunky.hero.domain.Hero;
import com.mlbbchunky.hero.domain.HeroRole;
import com.mlbbchunky.hero.domain.Lane;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DraftAssistantServiceTest {

    @Test
    void ranks_lane_fit_counter_and_synergy_above_generic_meta_pick() {
        Hero ideal = new Hero(
                10, "Ideal", null, Set.of(HeroRole.MARKSMAN), Set.of(Lane.GOLD),
                0.51, 0.05, 0.02, Set.of(99L), Set.of(50L)
        );
        Hero metaOnly = new Hero(
                11, "MetaOnly", null, Set.of(HeroRole.MAGE), Set.of(Lane.MID),
                0.56, 0.08, 0.10, Set.of(), Set.of()
        );

        HeroCatalog catalog = new HeroCatalog() {
            @Override
            public List<Hero> findAll() {
                return List.of(metaOnly, ideal);
            }

            @Override
            public Optional<Hero> findById(long id) {
                return findAll().stream().filter(hero -> hero.id() == id).findFirst();
            }
        };

        DraftAssistantService service = new DraftAssistantService(catalog);
        DraftRequest request = new DraftRequest(
                Lane.GOLD,
                HeroRole.MARKSMAN,
                Set.of(50L),
                Set.of(99L),
                Set.of()
        );

        var recommendations = service.recommend(request);

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations.getFirst().heroName()).isEqualTo("Ideal");
        assertThat(recommendations.getFirst().breakdown().counterScore()).isEqualTo(8.0);
        assertThat(recommendations.getFirst().breakdown().synergyScore()).isEqualTo(5.0);
        assertThat(recommendations.getFirst().reasons())
                .contains("Fits GOLD lane", "Counters one or more enemy picks", "Has synergy with an allied pick");
    }

    @Test
    void excludes_banned_and_already_selected_heroes() {
        Hero one = new Hero(1, "One", null, Set.of(HeroRole.TANK), Set.of(Lane.ROAM), 0.50, 0, 0, Set.of(), Set.of());
        Hero two = new Hero(2, "Two", null, Set.of(HeroRole.TANK), Set.of(Lane.ROAM), 0.50, 0, 0, Set.of(), Set.of());
        Hero three = new Hero(3, "Three", null, Set.of(HeroRole.TANK), Set.of(Lane.ROAM), 0.50, 0, 0, Set.of(), Set.of());

        HeroCatalog catalog = new HeroCatalog() {
            @Override public List<Hero> findAll() { return List.of(one, two, three); }
            @Override public Optional<Hero> findById(long id) { return Optional.empty(); }
        };

        var result = new DraftAssistantService(catalog).recommend(
                new DraftRequest(Lane.ROAM, HeroRole.TANK, Set.of(1L), Set.of(2L), Set.of(3L))
        );

        assertThat(result).isEmpty();
    }
}
