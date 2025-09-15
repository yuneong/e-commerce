package com.loopers.domain.ranking;

import com.loopers.dto.ProductMetricsSummary;
import com.loopers.dto.RankedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingCalculator {

    @Value("${ranking.weights.like}")
    private double likeWeight;

    @Value("${ranking.weights.order}")
    private double orderWeight;

    @Value("${ranking.weights.view}")
    private double viewWeight;

    public RankedProduct weightedSum(ProductMetricsSummary summary) {
        double score = likeWeight * summary.getLikeCount() +
                orderWeight * summary.getStockCount() +
                viewWeight * summary.getViewCount();

        return new RankedProduct(summary.getProductId(), score);
    }

}
