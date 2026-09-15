package com.mlbbchunky.hero.infrastructure;

import com.mlbbchunky.hero.application.HeroIngestionRepository;
import com.mlbbchunky.hero.application.MlbbHeroDataProvider.HeroData;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcHeroIngestionRepository implements HeroIngestionRepository {
    private final JdbcClient jdbc;

    public JdbcHeroIngestionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void upsertCatalog(List<HeroData> heroes) {
        for (HeroData hero : heroes) {
            jdbc.sql("""
                    insert into hero (id, name, image_url, updated_at)
                    values (:id, :name, :imageUrl, now())
                    on conflict (id) do update
                    set name = excluded.name,
                        image_url = excluded.image_url,
                        updated_at = now()
                    """)
                    .param("id", hero.id())
                    .param("name", hero.name())
                    .param("imageUrl", hero.imageUrl())
                    .update();

            jdbc.sql("delete from hero_role where hero_id = :heroId")
                    .param("heroId", hero.id())
                    .update();
            for (var role : hero.roles()) {
                jdbc.sql("insert into hero_role (hero_id, role) values (:heroId, :role)")
                        .param("heroId", hero.id())
                        .param("role", role.name())
                        .update();
            }

            jdbc.sql("delete from hero_lane where hero_id = :heroId")
                    .param("heroId", hero.id())
                    .update();
            for (var lane : hero.lanes()) {
                jdbc.sql("insert into hero_lane (hero_id, lane) values (:heroId, :lane)")
                        .param("heroId", hero.id())
                        .param("lane", lane.name())
                        .update();
            }
        }
    }
}
