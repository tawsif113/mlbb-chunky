package com.mlbbchunky.draft.api;

import java.util.List;

public record HeroRecommendation(
        long heroId,
        String heroName,
        double score,
        ScoreBreakdown breakdown,
        List<String> reasons
) {
    public record ScoreBreakdown(
            double laneFit,
            double roleFit,
            double metaScore,
            double counterScore,
            double synergyScore
    ) {}
}
