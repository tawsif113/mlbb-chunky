package com.mlbbchunky.hero.application;

import com.mlbbchunky.hero.domain.Hero;
import java.util.List;
import java.util.Optional;

public interface HeroCatalog {
    List<Hero> findAll();
    Optional<Hero> findById(long id);
}
