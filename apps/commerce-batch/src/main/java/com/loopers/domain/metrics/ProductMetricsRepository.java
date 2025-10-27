package com.loopers.domain.metrics;

import com.loopers.dto.ProductMetricsSummary;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductMetricsRepository {

    List<ProductMetricsSummary> findByIdProductIdAndDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
