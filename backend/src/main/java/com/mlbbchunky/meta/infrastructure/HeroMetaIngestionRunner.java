package com.mlbbchunky.meta.infrastructure;

import com.mlbbchunky.meta.application.HeroMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@ConditionalOnProperty(prefix = "app.mlbb", name = "meta-sync-on-startup", havingValue = "true")
public class HeroMetaIngestionRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(HeroMetaIngestionRunner.class);

    private final HeroMetaService service;
    private final String rankScope;
    private final int periodDays;

    public HeroMetaIngestionRunner(
            HeroMetaService service,
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
                    "MLBB meta synchronized: fetched={}, persisted={}, rank={}, period={}d at {}",
                    result.fetched(),
                    result.persisted(),
                    result.rankScope(),
                    result.periodDays(),
                    result.capturedAt()
            );
        } catch (RuntimeException failure) {
            log.error(
                    "MLBB meta startup sync failed for rank={} period={}d; Chunky will stay online and use the latest stored snapshot: {}",
                    rankScope,
                    periodDays,
                    failure.getMessage(),
                    failure
            );
        }
    }
}
