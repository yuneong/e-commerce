package com.loopers.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.common.UserActionEnvelope;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionEventProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.user-action-name}")
    private String userActionTopic;

    @Retry(name = "kafkaProducer", fallbackMethod = "userActionFallback")
    public void sendUserActionEvent(UserActionEnvelope<?> event)  {
        try {
            String kafkaJson = objectMapper.writeValueAsString(event);

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(userActionTopic, event.eventId(), kafkaJson);
            record.headers().add("eventType", "userAction".getBytes(StandardCharsets.UTF_8));

            kafkaTemplate.send(record);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user action event: {}", event, e);
        }
    }

    // fallback
    public void userActionFallback(Object event, Throwable ex) {
        log.error("Failed to send user action event after retries", ex);
    }

}
