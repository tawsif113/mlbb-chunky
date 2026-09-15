package com.mlbbchunky.hero.application;

import com.mlbbchunky.hero.domain.HeroRole;
import com.mlbbchunky.hero.domain.Lane;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HeroIngestionServiceTest {

    @Test
    void imports_provider_catalog_into_repository() {
        var expected = List.of(
                new MlbbHeroDataProvider.HeroData(
                        1,
                        "Miya",
                        "https://example.test/miya.jpg",
                        Set.of(HeroRole.MARKSMAN),
                        Set.of(Lane.GOLD)
                )
        );

        MlbbHeroDataProvider provider = () -> expected;
        List<MlbbHeroDataProvider.HeroData> persisted = new ArrayList<>();
        HeroIngestionRepository repository = heroes -> persisted.addAll(heroes);

        HeroIngestionService service = new HeroIngestionService(Optional.of(provider), repository);
        HeroIngestionService.SyncResult result = service.sync();

        assertThat(result.importedHeroes()).isEqualTo(1);
        assertThat(persisted).containsExactlyElementsOf(expected);
    }
}
