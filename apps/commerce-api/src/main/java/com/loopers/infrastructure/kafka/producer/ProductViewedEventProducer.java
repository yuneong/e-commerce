package com.loopers.infrastructure.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductViewedEventProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${kafka.topic.product-view-name}")
    private String productViewedTopic;

    public void sendProductViewedEvent(Long productId) {
        kafkaTemplate.send(productViewedTopic, productId.toString(), productId);
    }

}
