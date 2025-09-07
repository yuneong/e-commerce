package com.loopers.infrastructure.kafka.producer;

import com.loopers.infrastructure.kafka.dto.ProductViewedDto;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductViewedEventProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${kafka.topic.product-view-name}")
    private String productViewedTopic;

    @Retry(name = "kafkaProducer", fallbackMethod = "productViewedFallback")
    public void sendProductViewedEvent(Long productId) {
        ProductViewedDto event = ProductViewedDto.of(productId);
        kafkaTemplate.send(productViewedTopic, productId.toString(), event);
    }

    // fallback
    public void productViewedFallback(Long productId, Throwable ex) {
        log.error("Failed to send product view event after retries, productId={}", productId, ex);
    }

}
