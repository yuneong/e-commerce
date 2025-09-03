package com.loopers.domain.metrics;


import java.util.Optional;

public interface ProductMetricsRepository {

    Optional<ProductMetrics> findById(ProductMetricsId id);

    ProductMetrics save(ProductMetrics productMetrics);

}
