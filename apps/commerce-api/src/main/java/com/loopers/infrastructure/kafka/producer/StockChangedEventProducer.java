package com.loopers.infrastructure.kafka.producer;

import com.loopers.infrastructure.kafka.dto.StockChangedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockChangedEventProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${kafka.topic.product-stock-name}")
    private String stockChangedTopic;

    public void sendStockChangedEvent(Long productId, int stock, String changedType) {
        StockChangedDto event = StockChangedDto.of(productId, stock, changedType);
        kafkaTemplate.send(stockChangedTopic, productId.toString(), event);
    }

}
