package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.metrics.ProductMetricsCommand;
import com.loopers.interfaces.dto.ProductLikePayload;
import com.loopers.application.metrics.ProductMetricsFacade;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.interfaces.dto.ProductStockPayload;
import com.loopers.interfaces.dto.ProductViewPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMetricsConsumer {

    private final ProductMetricsFacade productMetricsFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"product-like-metrics", "product-stock-metrics", "product-view-metrics"},
            groupId = "product-metrics-group",
            containerFactory = KafkaConfig.STRING_BATCH_LISTENER
    )
    public void listen(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws JsonProcessingException {

        log.info("@@@@ payload {}", payload);
        log.info("@@@@ topic {}", topic);

        if (topic == null) {
            log.warn("Received null topic for payload: {}", payload);
            return;
        }

        switch (topic) {
            case "product-like-metrics":
                ProductLikePayload likePayload = objectMapper.readValue(payload, ProductLikePayload.class);
                ProductMetricsCommand likeCommand = ProductMetricsCommand.from(likePayload);
                productMetricsFacade.processLikeMetrics(likeCommand);
                break;
            case "product-stock-metrics":
                ProductStockPayload stockPayload = objectMapper.readValue(payload, ProductStockPayload.class);
                ProductMetricsCommand stockCommand = ProductMetricsCommand.from(stockPayload);
                productMetricsFacade.processStockMetrics(stockCommand);
                break;
            case "product-view-metrics":
                ProductViewPayload viewPayload = objectMapper.readValue(payload, ProductViewPayload.class);
                ProductMetricsCommand viewCommand = ProductMetricsCommand.from(viewPayload);
                productMetricsFacade.processViewMetrics(viewCommand);
                break;
        }

    }

}
