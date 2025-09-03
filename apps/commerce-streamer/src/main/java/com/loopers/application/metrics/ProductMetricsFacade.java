package com.loopers.application.metrics;

import com.loopers.domain.metrics.ProductMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ProductMetricsFacade {

    private final ProductMetricsService productMetricsService;
    private final static LocalDate today = LocalDate.now();

    public void processLikeMetrics(Long productId, String likeType) {
        productMetricsService.processLikeMetrics(productId, likeType, today);
    }

    public void processStockMetrics(Long productId, int quantity, String changedType) {
        productMetricsService.processStockMetrics(productId, quantity, changedType, today);
    }

    public void processViewMetrics(Long productId) {
        productMetricsService.processViewMetrics(productId, today);
    }

}
