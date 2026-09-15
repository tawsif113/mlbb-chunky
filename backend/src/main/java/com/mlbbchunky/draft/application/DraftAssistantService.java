package com.mlbbchunky.draft.application;

import com.mlbbchunky.draft.api.DraftRequest;
import com.mlbbchunky.draft.api.HeroRecommendation;
import com.mlbbchunky.draft.api.HeroRecommendation.ScoreBreakdown;
import com.mlbbchunky.hero.application.HeroCatalog;
import com.mlbbchunky.hero.domain.Hero;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DraftAssistantService {
    private final HeroCatalog heroCatalog;

    public DraftAssistantService(HeroCatalog heroCatalog) {
        this.heroCatalog = heroCatalog;
    }

    public List<HeroRecommendation> recommend(DraftRequest request) {
        Set<Long> unavailable = new HashSet<>(request.bannedHeroIds());
        unavailable.addAll(request.alliedHeroIds());
        unavailable.addAll(request.enemyHeroIds());

        return heroCatalog.findAll().stream()
                .filter(hero -> !unavailable.contains(hero.id()))
                .map(hero -> score(hero, request))
                .sorted(Comparator.comparingDouble(HeroRecommendation::score).reversed())
                .limit(8)
                .toList();
    }

    private HeroRecommendation score(Hero hero, DraftRequest request) {
        double laneFit = hero.lanes().contains(request.lane()) ? 30.0 : 0.0;
        double roleFit = request.preferredRole() == null
                ? 5.0
                : hero.roles().contains(request.preferredRole()) ? 15.0 : 0.0;

        // Meta contributes up to 20 points: 14 from win rate, 3 from pick rate and 3 from ban pressure.
        // Rates are fractions (e.g. 0.523 = 52.3%).
        double winSignal = Math.max(0, Math.min(14, (hero.winRate() - 0.45) * 140));
        double pickSignal = Math.max(0, Math.min(3, hero.pickRate() * 100));
        double banSignal = Math.max(0, Math.min(3, hero.banRate() * 30));
        double metaScore = winSignal + pickSignal + banSignal;

        double counterScore = overlap(hero.strongAgainst(), request.enemyHeroIds()) * 8.0;
        double synergyScore = overlap(hero.synergizesWith(), request.alliedHeroIds()) * 5.0;

        ScoreBreakdown breakdown = new ScoreBreakdown(
                laneFit,
                roleFit,
                round(metaScore),
                counterScore,
                synergyScore
        );

        List<String> reasons = new ArrayList<>();
        if (laneFit > 0) reasons.add("Fits " + request.lane() + " lane");
        if (roleFit == 15) reasons.add("Matches preferred " + request.preferredRole() + " role");
        if (counterScore > 0) reasons.add("Counters one or more enemy picks");
        if (synergyScore > 0) reasons.add("Has synergy with an allied pick");
        if (hero.winRate() >= 0.52) reasons.add("Strong current win-rate signal");
        if (hero.pickRate() >= 0.03) reasons.add("Frequently picked in the current meta");
        if (hero.banRate() >= 0.10) reasons.add("High current ban pressure");

        double score = laneFit + roleFit + metaScore + counterScore + synergyScore;
        return new HeroRecommendation(hero.id(), hero.name(), round(score), breakdown, reasons);
    }

    private long overlap(Set<Long> a, Set<Long> b) {
        return a.stream().filter(b::contains).count();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
