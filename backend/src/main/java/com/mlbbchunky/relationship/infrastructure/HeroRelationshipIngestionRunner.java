package com.mlbbchunky.relationship.infrastructure;

import com.mlbbchunky.relationship.application.HeroRelationshipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
@ConditionalOnProperty(prefix = "app.mlbb", name = "relationship-sync-on-startup", havingValue = "true")
public class HeroRelationshipIngestionRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(HeroRelationshipIngestionRunner.class);

    private final HeroRelationshipService service;
    private final String rankScope;
    private final int periodDays;

    public HeroRelationshipIngestionRunner(
            HeroRelationshipService service,
            @Value("${app.mlbb.meta.rank-scope:all}") String rankScope,
            @Value("${app.mlbb.meta.period-days:7}") int periodDays
    ) {
        this.service = service;
        this.rankScope = rankScope;
        this.periodDays = periodDays;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            var result = service.sync(rankScope, periodDays);
            log.info(
                    "MLBB relationships synchronized: counters={}/{}, synergies={}/{}, fetched={}, persisted={}, rank={}, period={}d at {}",
                    result.completedCounterHeroes(),
                    result.catalogHeroes(),
                    result.completedSynergyHeroes(),
                    result.catalogHeroes(),
                    result.fetchedRelationships(),
                    result.persistedRelationships(),
                    result.rankScope(),
                    result.periodDays(),
                    result.completedAt()
            );
        } catch (RuntimeException failure) {
            log.error(
                    "MLBB relationship startup sync failed. Chunky will stay online and use any relationships already stored: {}",
                    failure.getMessage(),
                    failure
            );
        }
    }
}
