package com.mlbbchunky.meta.infrastructure;

import com.mlbbchunky.meta.application.HeroMetaRepository;
import com.mlbbchunky.meta.application.MlbbMetaDataProvider.HeroMetaData;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class JdbcHeroMetaRepository implements HeroMetaRepository {
    private final JdbcClient jdbc;

    public JdbcHeroMetaRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int appendSnapshots(
            List<HeroMetaData> snapshots,
            String rankScope,
            int periodDays,
            Instant capturedAt
    ) {
        int persisted = 0;
        for (HeroMetaData snapshot : snapshots) {
            persisted += jdbc.sql("""
                    insert into hero_meta_snapshot (
                        hero_id, rank_scope, period_days, pick_rate, ban_rate, win_rate, captured_at
                    )
                    select :heroId, :rankScope, :periodDays, :pickRate, :banRate, :winRate, :capturedAt
                    where exists (select 1 from hero where id = :heroId)
                    """)
                    .param("heroId", snapshot.heroId())
                    .param("rankScope", rankScope)
                    .param("periodDays", periodDays)
                    .param("pickRate", snapshot.pickRate())
                    .param("banRate", snapshot.banRate())
                    .param("winRate", snapshot.winRate())
                    .param("capturedAt", Timestamp.from(capturedAt))
                    .update();
        }
        return persisted;
    }

    @Override
    public List<HeroMetaSnapshot> findLatest(String rankScope, int periodDays) {
        return jdbc.sql("""
                select distinct on (m.hero_id)
                       m.hero_id,
                       h.name as hero_name,
                       h.image_url,
                       m.rank_scope,
                       m.period_days,
                       m.pick_rate,
                       m.ban_rate,
                       m.win_rate,
                       m.captured_at
                from hero_meta_snapshot m
                join hero h on h.id = m.hero_id
                where m.rank_scope = :rankScope
                  and m.period_days = :periodDays
                order by m.hero_id, m.captured_at desc
                """)
                .param("rankScope", rankScope)
                .param("periodDays", periodDays)
                .query((rs, rowNum) -> new HeroMetaSnapshot(
                        rs.getLong("hero_id"),
                        rs.getString("hero_name"),
                        rs.getString("image_url"),
                        rs.getString("rank_scope"),
                        rs.getInt("period_days"),
                        rs.getDouble("pick_rate"),
                        rs.getDouble("ban_rate"),
                        rs.getDouble("win_rate"),
                        rs.getTimestamp("captured_at").toInstant()
                ))
                .list();
    }
}
