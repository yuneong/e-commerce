package com.loopers.domain.metrics;

import com.loopers.dto.ProductMetricsSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductMetricsRepository extends JpaRepository<ProductMetrics, ProductMetricsId> {

    @Query("""
        SELECT new com.loopers.dto.ProductMetricsSummary(
            pm.id.productId,
            SUM(pm.likesDelta),
            SUM(pm.salesDelta),
            SUM(pm.viewsDelta)
        )
        FROM ProductMetrics pm
        WHERE pm.id.date BETWEEN :startDate AND :endDate
        GROUP BY pm.id.productId
    """)
    List<ProductMetricsSummary> findByIdMetricsDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
