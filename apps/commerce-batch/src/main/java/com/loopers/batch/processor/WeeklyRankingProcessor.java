package com.loopers.batch.processor;

import com.loopers.dto.ProductMetricsSummary;
import com.loopers.dto.RankedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class WeeklyRankingProcessor implements ItemProcessor<ProductMetricsSummary, RankedProduct> {

    @Value("${ranking.weights.like}")
    private double likeWeight;

    @Value("${ranking.weights.order}")
    private double orderWeight;

    @Value("${ranking.weights.view}")
    private double viewWeight;

    @Override
    public RankedProduct process(ProductMetricsSummary summary) {
        // 오늘 날짜와 비교해서 얼마나 오래된 데이터인지 계산
        long daysAgo = ChronoUnit.DAYS.between(summary.getDate(), LocalDate.now());

        double decayWeight = getDecayWeight(daysAgo);

        double baseScore = likeWeight * summary.getLikeCount()
                + orderWeight * summary.getStockCount()
                + viewWeight * summary.getViewCount();

        double finalScore = baseScore * decayWeight;

        return new RankedProduct(summary.getProductId(), finalScore);
    }

    private double getDecayWeight(long daysAgo) {
        return switch ((int) daysAgo) {
            case 0 -> 1.0;   // d+1 (어제)
            case 1 -> 0.9;   // d+2
            case 2 -> 0.8;   // d+3
            // ----------------------------- 여기까지만 최근 데이터라고 판단
            case 3 -> 0.4;   // d+4
            case 4 -> 0.3;   // d+5
            case 5 -> 0.2;   // d+6
            case 6 -> 0.1;   // d+7
            default -> 0.0;  // 7일 초과는 무시
        };
    }

}
