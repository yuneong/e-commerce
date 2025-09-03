package com.loopers.interfaces.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.common.UserActionEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionEventListener {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.user-action-name}")
    private String userActionTopic;

    @EventListener
    public void handle(UserActionEnvelope<?> event) {
        try {
            // 사용자 행위 로깅
//            String payloadJson = objectMapper.writeValueAsString(event.payload());
//            log.info("actionTypetion={} payload={} at={}", event.actionType(), payloadJson, event.occurredAt());

            // 카프카 사용자 행위 로깅
            String kafkaJson = objectMapper.writeValueAsString(event);

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(userActionTopic, event.eventId(), kafkaJson);
            record.headers().add("eventType", "userAction".getBytes(StandardCharsets.UTF_8));

            kafkaTemplate.send(record);
        } catch (Exception e) {
            log.error("Failed to log user action: {}", event, e);
        }
    }

}
