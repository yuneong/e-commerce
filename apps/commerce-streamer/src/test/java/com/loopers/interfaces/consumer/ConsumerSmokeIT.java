package com.loopers.interfaces.consumer;

import com.loopers.application.metrics.ProductMetricsFacade;
import com.loopers.domain.auditlog.AuditLogRepository;
import com.loopers.domain.eventHandled.EventHandledRepository;
import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsId;
import com.loopers.domain.metrics.ProductMetricsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

import org.awaitility.Awaitility;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConsumerSmokeIT {

    @Autowired KafkaTemplate<String, String> kafkaTemplate;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired ProductMetricsRepository productMetricsRepository;
    @Autowired EventHandledRepository eventHandledRepository;
    @Autowired ProductMetricsFacade productMetricsFacade;

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(
                    Instant.parse("2025-09-04T15:00:00Z"), // 9시간 전
                    ZoneId.of("Asia/Seoul")
            );
        }
    }

    @Value("${kafka.topic.product-like-name:product-like-metrics}")
    String LIKE_TOPIC;

    @Value("${kafka.topic.user-action-name:user-action-events}")
    String USER_ACTION_TOPIC;


    @DisplayName("userAction 이벤트 처리")
    @Nested
    class userActionEvent {
        @Test
        @DisplayName("producer -> kafka → consumer → facade → service → db : user-action-events 1건 적재")
        void user_action_smoke() {
            // given
            String eventId = "evt-audit-smoke";
            String traceId = "dfsdfwefsdfsdfsf";
            String userId  = "oyy";
            String action  = "PRODUCT_LIKE";
            String occurredAt = LocalDate.now().atStartOfDay().toString();

            String payload = """
                {
                  "eventId":"%s",
                  "eventType":"userAction",
                  "traceId":"%s",
                  "userId":"%s",
                  "actionType":"%s",
                  "payload":{"productId":100, "likeType":"like"},
                  "occurredAt":"%s"
                }
            """.formatted(eventId, traceId, userId, action, occurredAt);

            // when
            kafkaTemplate.send(USER_ACTION_TOPIC, traceId, payload);
            kafkaTemplate.flush();

            // then
            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                assertThat(eventHandledRepository.existsByEventId(eventId)).isTrue();

                if (auditLogRepository != null) {
                    assertThat(auditLogRepository.existsByEventId(eventId)).isTrue();
                }
            });
        }
    }

//    @DisplayName("ProductMetrics 이벤트 처리")
//    @Nested
//    class productMetricsEvent {
//        @Test
//        @DisplayName("producer -> kafka → consumer → facade → service → db : like 1건 반영")
//        void like_smoke() {
//            String eventId = "evt-like-smoke";
//            long productId = 100L;
//
//            String payload = """
//                {"eventId":"%s", "productId":%d, "likeType":"like"}
//            """.formatted(eventId, productId);
//
//            kafkaTemplate.send(LIKE_TOPIC, String.valueOf(productId), payload);
//            kafkaTemplate.flush();
//
//            LocalDate today = productMetricsFacade.today();
//            ProductMetricsId metricsId = ProductMetricsId.create(productId, today);
//
//            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
//                assertThat(eventHandledRepository.existsByEventId(eventId)).isTrue();
//
//                if (productMetricsRepository != null) {
//                    assertThat(productMetricsRepository.findById(metricsId)).isPresent();
//                }
//            });
//        }
//
//    }

}
