package com.loopers.domain.ranking;

import com.loopers.application.metrics.MetricsCounter;
import org.springframework.beans.factory.annotation.Value;

public class RankingCalculator {

    @Value("${ranking.weights.like}")
    private static double likeWeight;

    @Value("${ranking.weights.order}")
    private static double orderWeight;

    @Value("${ranking.weights.view}")
    private static double viewWeight;

    public static double weightedSum(MetricsCounter counter) {
        return likeWeight * counter.getLikeCount() +
               orderWeight * counter.getStockCount() +
               viewWeight * counter.getViewCount();
    }

}
