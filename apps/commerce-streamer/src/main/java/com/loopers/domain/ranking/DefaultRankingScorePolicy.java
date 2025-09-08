package com.loopers.domain.ranking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultRankingScorePolicy implements RankingScorePolicy {

    @Value("${ranking.score.like}")
    private double likeScore;

    @Value("${ranking.score.order}")
    private double orderScore;

    @Value("${ranking.score.view}")
    private double viewScore;

    @Override
    public double scoreFor(RankingEventType type) {
        return switch (type) {
            case LIKE -> likeScore;
            case ORDER -> orderScore;
            case VIEW -> viewScore;
        };
    }

}
