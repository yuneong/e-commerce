package com.loopers.application.auditlog;

import com.loopers.domain.auditlog.AuditLogRepository;
import com.loopers.domain.eventHandled.EventHandledRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditLogFacadeIntegrationTest {

    @Autowired
    AuditLogFacade auditLogFacade;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    EventHandledRepository eventHandledRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


//    @Test
//    void testProduceAndConsume() throws Exception {
//        // Testcontainers Config에서 세팅한 값 가져오기
//        Map<String, Object> consumerProps = new HashMap<>();
//        consumerProps.put("bootstrap.servers", "localhost:19092");
//        consumerProps.put("group.id", "console-consumer-59115");
//        consumerProps.put("enable.auto.commit", "true");
//        consumerProps.put("auto.offset.reset", "latest");
//        consumerProps.put("key.deserializer", StringDeserializer.class);
//        consumerProps.put("value.deserializer", StringDeserializer.class);
//
//        try (Consumer<String, String> consumer =
//                     new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer())) {
//
//            consumer.subscribe(Collections.singletonList("test-topic"));
//
//            // 메시지 발행 (동기)
//            kafkaTemplate.send("test-topic", "hello").get();
//
//            // 메시지 읽기
//            ConsumerRecord<String, String> record =
//                    KafkaTestUtils.getSingleRecord(consumer, "test-topic", Duration.ofSeconds(5));
//
//            assertThat(record.value()).isEqualTo("hello");
//        }
//    }

    @Test
    @DisplayName("새로운 이벤트 → AuditLog + EventHandled 저장")
    void processAuditLog_success() {
        // given
        String eventId = "evt-audit-123";
        Map<String, Object> payload = Map.of(
                "orderId", 4,
                "paymentId", 2,
                "totalPrice", 40000,
                "paymentMethod", "POINT"
        );
        AuditLogCommand command = new AuditLogCommand(
                eventId,
                "userAction",
                "user-100",
                "oyy",
                "PAYMENT_PROCESS",
                payload,
                LocalDateTime.now()
        );

        // when
        auditLogFacade.processAuditLog(command);
        auditLogFacade.processAuditLog(command);

        // then
        assertThat(auditLogRepository.findAll())
                .hasSize(1)
                .anySatisfy(log -> {
                    assertThat(log.getEventId()).isEqualTo(eventId);
                    assertThat(log.getEventType()).isEqualTo("userAction");
                    assertThat(log.getUserId()).isEqualTo("oyy");
                });

        assertThat(eventHandledRepository.existsByEventId(eventId)).isTrue();
    }

}
