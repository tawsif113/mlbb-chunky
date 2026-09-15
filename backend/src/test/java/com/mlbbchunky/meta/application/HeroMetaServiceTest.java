package com.mlbbchunky.meta.application;

import com.mlbbchunky.meta.application.HeroMetaRepository.HeroMetaSnapshot;
import com.mlbbchunky.meta.application.MlbbMetaDataProvider.HeroMetaData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeroMetaServiceTest {

    @Test
    void fetches_and_persists_requested_meta_snapshot() {
        MlbbMetaDataProvider provider = (rankScope, periodDays) -> List.of(
                new HeroMetaData(1, 0.07, 0.03, 0.52),
                new HeroMetaData(2, 0.04, 0.08, 0.54)
        );
        RecordingRepository repository = new RecordingRepository();
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("provider", provider);

        HeroMetaService service = new HeroMetaService(
                beans.getBeanProvider(MlbbMetaDataProvider.class),
                repository
        );

        var result = service.sync("MYTHIC", 7);

        assertThat(result.rankScope()).isEqualTo("mythic");
        assertThat(result.periodDays()).isEqualTo(7);
        assertThat(result.fetched()).isEqualTo(2);
        assertThat(result.persisted()).isEqualTo(2);
        assertThat(repository.saved).hasSize(2);
        assertThat(repository.rankScope).isEqualTo("mythic");
    }

    @Test
    void rejects_unsupported_periods_before_calling_provider() {
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        HeroMetaService service = new HeroMetaService(
                beans.getBeanProvider(MlbbMetaDataProvider.class),
                new RecordingRepository()
        );

        assertThatThrownBy(() -> service.latest("all", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("periodDays");
    }

    private static final class RecordingRepository implements HeroMetaRepository {
        private final List<HeroMetaData> saved = new ArrayList<>();
        private String rankScope;

        @Override
        public int appendSnapshots(
                List<HeroMetaData> snapshots,
                String rankScope,
                int periodDays,
                Instant capturedAt
        ) {
            this.saved.addAll(snapshots);
            this.rankScope = rankScope;
            return snapshots.size();
        }

        @Override
        public List<HeroMetaSnapshot> findLatest(String rankScope, int periodDays) {
            return List.of();
        }
    }
}
