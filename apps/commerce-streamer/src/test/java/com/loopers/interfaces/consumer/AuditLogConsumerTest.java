package com.loopers.interfaces.consumer;

import com.loopers.application.auditlog.AuditLogCommand;
import com.loopers.application.auditlog.AuditLogFacade;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuditLogConsumerTest {

    private final AuditLogFacade facade = mock(AuditLogFacade.class);
    private final AuditLogConsumer consumer = new AuditLogConsumer(facade);

    private ConsumerRecord<String, String> makeRecord(
            String topic, String key, String value, String eventTypeHeader
    ) {
        ConsumerRecord<String, String> rec = new ConsumerRecord<>(topic, 0, 0L, key, value);
        if (eventTypeHeader != null) {
            rec.headers().add("eventType", eventTypeHeader.getBytes(StandardCharsets.UTF_8));
        }
        return rec;
    }

    @Test
    @DisplayName("userAction 토픽 → JSON 파싱 → Facade.processAuditLog 호출")
    void listen_withMapPayload() {
        // given
        String topic = "user-action-events";
        String userId = "oyy";

        String jsonValue = """
          {
            "eventId": "evt-audit-123",
            "traceId": "sdwers3rdgsdf",
            "userId": "oyy",
            "actionType": "PAYMENT_PROCESS",
            "payload": {
              "orderId": 4,
              "paymentId": 2,
              "totalPrice": 40000,
              "paymentMethod": "POINT"
            },
            "occurredAt": "2025-09-06T12:34:56"
          }
        """;

        ConsumerRecord<String, String> record = makeRecord(topic, userId, jsonValue, "userAction");
        List<ConsumerRecord<String, String>> records = List.of(record);


        // when
        consumer.listen(records);

        // then
        ArgumentCaptor<AuditLogCommand> captor = ArgumentCaptor.forClass(AuditLogCommand.class);
        verify(facade, times(1)).processAuditLog(captor.capture());

        AuditLogCommand actual = captor.getValue();
        assertThat(actual.eventId()).isEqualTo("evt-audit-123");
        assertThat(actual.eventType()).isEqualTo("userAction"); // 헤더에서 주입됨
        assertThat(actual.userId()).isEqualTo("oyy");
        assertThat(actual.traceId()).isEqualTo("sdwers3rdgsdf");
        assertThat(actual.payload()).containsEntry("paymentMethod", "POINT");
        assertThat(actual.payload()).containsEntry("totalPrice", 40000);
    }

}
