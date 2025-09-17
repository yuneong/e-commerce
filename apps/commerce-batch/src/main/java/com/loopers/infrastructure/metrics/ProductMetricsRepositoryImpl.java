package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetricsRepository;
import com.loopers.dto.ProductMetricsSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {

    private final ProductMetricsJpaRepository productMetricsJpaRepository;

    @Override
    public List<ProductMetricsSummary> findByIdProductIdAndDateBetween(LocalDate startDate, LocalDate endDate) {
        return productMetricsJpaRepository.findByIdProductIdAndDateBetween(startDate, endDate);
    }

}
