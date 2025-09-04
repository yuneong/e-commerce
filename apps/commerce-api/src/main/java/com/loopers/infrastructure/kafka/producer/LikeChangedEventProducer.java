package com.loopers.infrastructure.kafka.producer;

import com.loopers.infrastructure.kafka.dto.LikeChangedDto;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeChangedEventProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${kafka.topic.product-like-name}")
    private String likeChangedTopic;

    @Retry(name = "kafkaProducer", fallbackMethod = "likeChangedFallback")
    public void sendLikeChangedEvent(Long productId, String likeType) {
        LikeChangedDto event = LikeChangedDto.of(productId, likeType);
        kafkaTemplate.send(likeChangedTopic, productId.toString(), event);
    }

    // fallback
    public void likeChangedFallback(Long productId, String likeType, Throwable ex) {
        log.error("Failed to send like-changed event after retries, productId={}, likeType={}", productId, likeType, ex);
    }

}
