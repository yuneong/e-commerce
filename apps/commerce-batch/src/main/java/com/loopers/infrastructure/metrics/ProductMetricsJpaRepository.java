package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsId;
import com.loopers.dto.ProductMetricsSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, ProductMetricsId> {

    @Query("""
        SELECT new com.loopers.dto.ProductMetricsSummary(
            pm.id.productId,
            pm.id.date,
            SUM(pm.likesDelta),
            SUM(pm.salesDelta),
            SUM(pm.viewsDelta)
        )
        FROM ProductMetrics pm
        WHERE pm.id.date BETWEEN :startDate AND :endDate
        GROUP BY pm.id.productId, pm.id.date
    """)
    List<ProductMetricsSummary> findByIdProductIdAndDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
