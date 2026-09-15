package com.mlbbchunky.relationship.infrastructure;

import com.mlbbchunky.relationship.application.HeroRelationshipRepository;
import com.mlbbchunky.relationship.application.MlbbRelationshipDataProvider.FetchResult;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcHeroRelationshipRepository implements HeroRelationshipRepository {
    private final JdbcClient jdbc;

    public JdbcHeroRelationshipRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Long> findHeroIds() {
        return jdbc.sql("select id from hero order by id")
                .query(Long.class)
                .list();
    }

    @Override
    public int replaceRelationships(FetchResult result, String rankScope, int periodDays) {
        for (var completed : result.completedSubjects()) {
            jdbc.sql("""
                    delete from hero_relationship
                    where subject_hero_id = :subjectHeroId
                      and relationship_type = :relationshipType
                      and rank_scope = :rankScope
                      and period_days = :periodDays
                    """)
                    .param("subjectHeroId", completed.subjectHeroId())
                    .param("relationshipType", completed.type().name())
                    .param("rankScope", rankScope)
                    .param("periodDays", periodDays)
                    .update();
        }

        int persisted = 0;
        for (var relationship : result.relationships()) {
            persisted += jdbc.sql("""
                    insert into hero_relationship (
                        subject_hero_id,
                        related_hero_id,
                        relationship_type,
                        rank_scope,
                        period_days,
                        impact,
                        updated_at
                    ) values (
                        :subjectHeroId,
                        :relatedHeroId,
                        :relationshipType,
                        :rankScope,
                        :periodDays,
                        :impact,
                        now()
                    )
                    on conflict (
                        subject_hero_id,
                        related_hero_id,
                        relationship_type,
                        rank_scope,
                        period_days
                    ) do update
                    set impact = excluded.impact,
                        updated_at = now()
                    """)
                    .param("subjectHeroId", relationship.subjectHeroId())
                    .param("relatedHeroId", relationship.relatedHeroId())
                    .param("relationshipType", relationship.type().name())
                    .param("rankScope", rankScope)
                    .param("periodDays", periodDays)
                    .param("impact", relationship.impact())
                    .update();
        }
        return persisted;
    }
}
