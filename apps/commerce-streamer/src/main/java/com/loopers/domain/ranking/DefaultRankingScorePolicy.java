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
    public double scoreFor(RankingEventType type, RankingContext context) {
        double score = switch (type) {
            case LIKE, VIEW -> 1.0;
            case ORDER -> {
                int price = context.price() != 0 ? context.price() : 0;
                int amount = context.amount() != 0 ? context.amount() : 0;
                yield price * amount;
            }
        };

        double weight = switch (type) {
            case LIKE -> likeScore;
            case ORDER -> orderScore;
            case VIEW -> viewScore;
        };

        return weight * score;
    }

}
