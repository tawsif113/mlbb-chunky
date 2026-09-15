package com.mlbbchunky.relationship.application;

import com.mlbbchunky.relationship.application.MlbbRelationshipDataProvider.CompletedSubject;
import com.mlbbchunky.relationship.application.MlbbRelationshipDataProvider.FetchResult;
import com.mlbbchunky.relationship.application.MlbbRelationshipDataProvider.HeroRelationshipData;
import com.mlbbchunky.relationship.application.MlbbRelationshipDataProvider.RelationshipType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HeroRelationshipServiceTest {

    @Test
    void synchronizes_completed_counter_and_synergy_subjects() {
        MlbbRelationshipDataProvider provider = (heroIds, rankScope, periodDays) -> new FetchResult(
                List.of(
                        new HeroRelationshipData(10, 20, RelationshipType.COUNTER, -0.04),
                        new HeroRelationshipData(10, 30, RelationshipType.SYNERGY, 0.03)
                ),
                Set.of(
                        new CompletedSubject(10, RelationshipType.COUNTER),
                        new CompletedSubject(10, RelationshipType.SYNERGY)
                )
        );

        class FakeRepository implements HeroRelationshipRepository {
            FetchResult saved;

            @Override
            public List<Long> findHeroIds() {
                return List.of(10L, 20L, 30L);
            }

            @Override
            public int replaceRelationships(FetchResult result, String rankScope, int periodDays) {
                saved = result;
                return result.relationships().size();
            }
        }

        FakeRepository repository = new FakeRepository();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("relationshipProvider", provider);

        HeroRelationshipService service = new HeroRelationshipService(
                beanFactory.getBeanProvider(MlbbRelationshipDataProvider.class),
                repository
        );

        var result = service.sync("ALL", 7);

        assertThat(result.catalogHeroes()).isEqualTo(3);
        assertThat(result.completedCounterHeroes()).isEqualTo(1);
        assertThat(result.completedSynergyHeroes()).isEqualTo(1);
        assertThat(result.persistedRelationships()).isEqualTo(2);
        assertThat(repository.saved.relationships()).hasSize(2);
    }
}
