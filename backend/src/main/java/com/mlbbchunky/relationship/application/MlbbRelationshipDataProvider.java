package com.mlbbchunky.relationship.application;

import java.util.List;
import java.util.Set;

public interface MlbbRelationshipDataProvider {

    FetchResult fetchRelationships(List<Long> heroIds, String rankScope, int periodDays);

    enum RelationshipType {
        COUNTER,
        SYNERGY
    }

    record HeroRelationshipData(
            long subjectHeroId,
            long relatedHeroId,
            RelationshipType type,
            Double impact
    ) {}

    record CompletedSubject(
            long subjectHeroId,
            RelationshipType type
    ) {}

    record FetchResult(
            List<HeroRelationshipData> relationships,
            Set<CompletedSubject> completedSubjects
    ) {}
}
