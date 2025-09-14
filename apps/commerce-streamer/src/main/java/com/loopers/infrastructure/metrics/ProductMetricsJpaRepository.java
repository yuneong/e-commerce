package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, ProductMetricsId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select pm
        from ProductMetrics pm
        where pm.id.productId = :productId
          and pm.id.date = :date
    """)
    ProductMetrics findByIdWithLock(
            @Param("productId") Long productId,
            @Param("date") LocalDate date
    );

}
