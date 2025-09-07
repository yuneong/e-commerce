package com.loopers.domain.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class ProductMetricsService {

    private final ProductMetricsRepository productMetricsRepository;

    @Transactional
    public void processLikeMetrics(Long productId, String likeType, LocalDate date) {
        ProductMetrics metrics = productMetricsRepository.findById(ProductMetricsId.create(productId, date))
                .orElseGet(() -> ProductMetrics.create(productId, date));
        metrics.increaseLike(Objects.equals(likeType, "like") ? 1 : -1);
        productMetricsRepository.save(metrics);
    }

    @Transactional
    public void processStockMetrics(Long productId, int quantity, String changedType, LocalDate date) {
        ProductMetrics metrics = productMetricsRepository.findById(ProductMetricsId.create(productId, date))
                .orElseGet(() -> ProductMetrics.create(productId, date));
        if (changedType.equals("SUCCESS")) {
            metrics.increaseSales(quantity); // 결제 성공: 판매량 증가
        } else if (changedType.equals("FAIL")) {
            metrics.increaseSales(-quantity); // 취소/실패: 판매량 감소
        }
        productMetricsRepository.save(metrics);
    }

    @Transactional
    public void processViewMetrics(Long productId, LocalDate date) {
        ProductMetrics metrics = productMetricsRepository.findById(ProductMetricsId.create(productId, date))
                .orElseGet(() -> ProductMetrics.create(productId, date));
        metrics.increaseView(1);
        productMetricsRepository.save(metrics);
    }

}
