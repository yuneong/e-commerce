package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.metrics.MetricsCounter;
import com.loopers.application.metrics.ProductMetricsCommand;
import com.loopers.application.ranking.RankingFacade;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMetricsConsumer {

    private final ProductMetricsFacade productMetricsFacade;
    private final ObjectMapper objectMapper;
    private final RankingFacade rankingFacade;

    @KafkaListener(
            topics = {"product-like-metrics", "product-stock-metrics", "product-view-metrics"},
            groupId = "product-metrics-group",
            containerFactory = KafkaConfig.STRING_BATCH_LISTENER
    )
    public void listen(
            List<String> payloads,
            @Header(KafkaHeaders.RECEIVED_TOPIC) List<String> topics
    ) throws JsonProcessingException {

        Map<Long, MetricsCounter> counters = new HashMap<>();

        for (int i = 0; i < payloads.size(); i++) {
            String payload = payloads.get(i);
            String topic = topics.get(i);

            if (topic == null) {
                log.warn("Received null topic for payload: {}", payload);
                return;
            }

            switch (topic) {
                case "product-like-metrics":
                    ProductLikePayload likePayload = objectMapper.readValue(payload, ProductLikePayload.class);
                    ProductMetricsCommand likeCommand = ProductMetricsCommand.from(likePayload);
                    int likeMetrics = productMetricsFacade.processLikeMetrics(likeCommand);
                    counters.computeIfAbsent(likeCommand.productId(), k -> new MetricsCounter()).addLike(likeMetrics);
                    break;
                case "product-stock-metrics":
                    ProductStockPayload stockPayload = objectMapper.readValue(payload, ProductStockPayload.class);
                    ProductMetricsCommand stockCommand = ProductMetricsCommand.from(stockPayload);
                    int stockMetrics = productMetricsFacade.processStockMetrics(stockCommand);
                    counters.computeIfAbsent(stockCommand.productId(), k -> new MetricsCounter()).addStock(stockMetrics);
                    break;
                case "product-view-metrics":
                    ProductViewPayload viewPayload = objectMapper.readValue(payload, ProductViewPayload.class);
                    ProductMetricsCommand viewCommand = ProductMetricsCommand.from(viewPayload);
                    int viewMetrics = productMetricsFacade.processViewMetrics(viewCommand);
                    counters.computeIfAbsent(viewCommand.productId(), k -> new MetricsCounter()).addView(viewMetrics);
                    break;
            }
        }

        // 랭킹 처리
        rankingFacade.processRanking(counters);
    }

}
