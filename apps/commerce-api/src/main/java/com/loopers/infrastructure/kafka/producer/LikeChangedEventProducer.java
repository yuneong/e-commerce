package com.loopers.infrastructure.kafka.producer;

import com.loopers.infrastructure.kafka.dto.LikeChangedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeChangedEventProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${kafka.topic.product-like-name}")
    private String likeChangedTopic;

    public void sendLikeChangedEvent(Long productId, String likeType) {
        LikeChangedDto event = LikeChangedDto.of(productId, likeType);
        kafkaTemplate.send(likeChangedTopic, productId.toString(), event);
    }

}
