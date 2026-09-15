package com.mlbbchunky.hero.infrastructure;

import com.mlbbchunky.hero.application.HeroIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
@ConditionalOnProperty(prefix = "app.mlbb", name = "hero-sync-on-startup", havingValue = "true")
public class HeroIngestionRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(HeroIngestionRunner.class);

    private final HeroIngestionService ingestionService;

    public HeroIngestionRunner(HeroIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        HeroIngestionService.SyncResult result = ingestionService.sync();
        log.info("MLBB hero catalog synchronized: {} heroes at {}", result.importedHeroes(), result.completedAt());
    }
}
