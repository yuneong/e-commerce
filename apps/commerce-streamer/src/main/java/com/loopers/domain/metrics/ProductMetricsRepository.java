package com.loopers.domain.metrics;


import java.util.Optional;

public interface ProductMetricsRepository {

    Optional<ProductMetrics> findById(ProductMetricsId id);

    Optional<ProductMetrics> findByIdWithLock(ProductMetricsId id);

    ProductMetrics save(ProductMetrics productMetrics);

}
