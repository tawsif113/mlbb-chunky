package com.mlbbchunky.hero.infrastructure;

import com.mlbbchunky.hero.application.HeroCatalog;
import com.mlbbchunky.hero.domain.Hero;
import com.mlbbchunky.hero.domain.HeroRole;
import com.mlbbchunky.hero.domain.Lane;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@Primary
public class DatabaseHeroCatalog implements HeroCatalog {
    private final JdbcClient jdbc;
    private final SeedHeroCatalog fallback;

    public DatabaseHeroCatalog(JdbcClient jdbc, SeedHeroCatalog fallback) {
        this.jdbc = jdbc;
        this.fallback = fallback;
    }

    @Override
    public List<Hero> findAll() {
        List<BaseHeroRow> heroes = jdbc.sql("""
                select h.id,
                       h.name,
                       h.image_url,
                       m.win_rate,
                       m.pick_rate,
                       m.ban_rate
                from hero h
                left join lateral (
                    select win_rate, pick_rate, ban_rate
                    from hero_meta_snapshot snapshot
                    where snapshot.hero_id = h.id
                    order by captured_at desc
                    limit 1
                ) m on true
                order by h.id
                """)
                .query((rs, rowNum) -> new BaseHeroRow(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("image_url"),
                        decimalOrDefault(rs.getBigDecimal("win_rate"), 0.50),
                        decimalOrDefault(rs.getBigDecimal("pick_rate"), 0.0),
                        decimalOrDefault(rs.getBigDecimal("ban_rate"), 0.0)
                ))
                .list();

        if (heroes.isEmpty()) {
            return fallback.findAll();
        }

        Map<Long, Set<HeroRole>> roles = new HashMap<>();
        jdbc.sql("select hero_id, role from hero_role")
                .query((rs, rowNum) -> new RoleRow(rs.getLong("hero_id"), HeroRole.valueOf(rs.getString("role"))))
                .list()
                .forEach(row -> roles.computeIfAbsent(row.heroId(), ignored -> new java.util.HashSet<>()).add(row.role()));

        Map<Long, Set<Lane>> lanes = new HashMap<>();
        jdbc.sql("select hero_id, lane from hero_lane")
                .query((rs, rowNum) -> new LaneRow(rs.getLong("hero_id"), Lane.valueOf(rs.getString("lane"))))
                .list()
                .forEach(row -> lanes.computeIfAbsent(row.heroId(), ignored -> new java.util.HashSet<>()).add(row.lane()));

        return heroes.stream()
                .map(row -> new Hero(
                        row.id(),
                        row.name(),
                        row.imageUrl(),
                        Set.copyOf(roles.getOrDefault(row.id(), Set.of())),
                        Set.copyOf(lanes.getOrDefault(row.id(), Set.of())),
                        row.winRate(),
                        row.pickRate(),
                        row.banRate(),
                        Set.of(),
                        Set.of()
                ))
                .toList();
    }

    @Override
    public Optional<Hero> findById(long id) {
        return findAll().stream().filter(hero -> hero.id() == id).findFirst();
    }

    private static double decimalOrDefault(BigDecimal value, double fallback) {
        return value == null ? fallback : value.doubleValue();
    }

    private record BaseHeroRow(
            long id,
            String name,
            String imageUrl,
            double winRate,
            double pickRate,
            double banRate
    ) {}

    private record RoleRow(long heroId, HeroRole role) {}

    private record LaneRow(long heroId, Lane lane) {}
}
