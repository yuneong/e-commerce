package com.loopers.domain.ranking;

import com.loopers.application.metrics.MetricsCounter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RankingCalculator {

    @Value("${ranking.weights.like}")
    private double likeWeight;

    @Value("${ranking.weights.order}")
    private double orderWeight;

    @Value("${ranking.weights.view}")
    private double viewWeight;

    public double weightedSum(MetricsCounter counter) {
        return likeWeight * counter.getLikeCount() +
               orderWeight * counter.getStockCount() +
               viewWeight * counter.getViewCount();
    }

}
