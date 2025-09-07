package com.loopers.application.metrics;

import com.loopers.domain.eventHandled.EventHandledRepository;
import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsId;
import com.loopers.domain.metrics.ProductMetricsRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductMetricsFacadeIntegrationTest {

    @Autowired ProductMetricsRepository productMetricsRepository;
    @Autowired EventHandledRepository eventHandledRepository;
    @MockitoSpyBean ProductMetricsFacade productMetricsFacade;

    private static final LocalDate FIXED_DATE = LocalDate.now();

    @Test
    @DisplayName("Kafka 중복 재전송(Like) → 좋아요는 한 번만 +1 (멱등)")
    void like_duplicate_idempotent() {
        // given
        String eventId = "evt-like-dup-test";
        long productId = 101L;

        ProductMetricsCommand command = new ProductMetricsCommand(
                eventId, productId, MetricsType.LIKE, "like",null, null
        );

        // when
        productMetricsFacade.processLikeMetrics(command);
        productMetricsFacade.processLikeMetrics(command);

        // then
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            productMetricsRepository.findById(ProductMetricsId.create(productId, FIXED_DATE))
                    .ifPresentOrElse(
                            metrics -> assertThat(metrics.getLikesDelta()).isEqualTo(1),
                            () -> {
                                throw new IllegalStateException("Metrics not found for productId: " + productId + " on date: " + FIXED_DATE);
                            }
                    );
            assertThat(eventHandledRepository.existsByEventId(eventId)).isTrue();
        });
    }

    @Test
    @DisplayName("Kafka 중복 재전송(Stock SUCCESS) → 판매량은 한 번만 +quantity (멱등)")
    void stock_duplicate_idempotent_success() {
        // given
        String eventId = "evt-stock-dup-success";
        long productId = 201L;
        int quantity = 3;

        ProductMetricsCommand command = new ProductMetricsCommand(
                eventId, productId, MetricsType.STOCK, null, quantity, "SUCCESS"
        );

        // when
        productMetricsFacade.processStockMetrics(command);
        productMetricsFacade.processStockMetrics(command);

        // then
        ProductMetricsId id = ProductMetricsId.create(productId, FIXED_DATE);
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(productMetricsRepository.findById(id)).isPresent();

            ProductMetrics metrics = productMetricsRepository.findById(id).orElseThrow();
            assertThat(metrics.getSalesDelta()).isEqualTo(quantity);

            assertThat(eventHandledRepository.existsByEventId(eventId)).isTrue();
        });
    }

    @Test
    @DisplayName("Kafka 중복 재전송(View) → 조회수는 한 번만 +1 (멱등)")
    void view_duplicate_idempotent() {
        // given
        String eventId = "evt-view-dup-test";
        long productId = 301L;

        ProductMetricsCommand command = new ProductMetricsCommand(
                eventId, productId, MetricsType.VIEW, null, null, null
        );

        // when
        productMetricsFacade.processViewMetrics(command);
        productMetricsFacade.processViewMetrics(command);

        // then
        ProductMetricsId id = ProductMetricsId.create(productId, FIXED_DATE);
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(productMetricsRepository.findById(id)).isPresent();

            ProductMetrics metrics = productMetricsRepository.findById(id).orElseThrow();
            assertThat(metrics.getViewsDelta()).isEqualTo(1);

            assertThat(eventHandledRepository.existsByEventId(eventId)).isTrue();
        });
    }


}
