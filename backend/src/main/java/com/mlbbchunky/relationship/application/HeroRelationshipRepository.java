package com.mlbbchunky.relationship.application;

import com.mlbbchunky.relationship.application.MlbbRelationshipDataProvider.FetchResult;

import java.util.List;

public interface HeroRelationshipRepository {

    List<Long> findHeroIds();

    int replaceRelationships(FetchResult result, String rankScope, int periodDays);
}
