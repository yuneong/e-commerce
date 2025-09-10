package com.loopers.application.metrics;

import com.loopers.domain.eventHandled.EventHandledDomainType;
import com.loopers.domain.eventHandled.EventHandledService;
import com.loopers.domain.metrics.ProductMetricsService;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ProductMetricsFacade {

    private final ProductMetricsService productMetricsService;
    private final EventHandledService eventHandledService;
    private final RankingService rankingService;
    private final Clock clock;

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    private final static EventHandledDomainType DOMAIN_TYPE = EventHandledDomainType.METRICS;

    public int processLikeMetrics(ProductMetricsCommand command) {
        if (eventHandledService.isEventHandled(command.eventId())) {
            return 0;
        }

        LocalDate date = today();
        int likeMetrics = productMetricsService.processLikeMetrics(command.productId(), command.likeType(), date);

        eventHandledService.saveEventHandled(command.eventId(), DOMAIN_TYPE, command.metricsType().toString());

        // 랭킹
        rankingService.recordLike(command.productId());

        return likeMetrics;
    }

    public int processStockMetrics(ProductMetricsCommand command) {
        if (eventHandledService.isEventHandled(command.eventId())) {
            return 0;
        }

        LocalDate date = today();
        int stockMetrics = productMetricsService.processStockMetrics(command.productId(), command.stock(), command.changedType(), date);

        eventHandledService.saveEventHandled(command.eventId(), DOMAIN_TYPE, command.metricsType().toString());

        // 랭킹
        rankingService.recordOrder(command.productId(), command.stock(), command.price());

        return stockMetrics;
    }

    public int processViewMetrics(ProductMetricsCommand command) {
        if (eventHandledService.isEventHandled(command.eventId())) {
            return 0;
        }

        LocalDate date = today();
        int viewMetrics = productMetricsService.processViewMetrics(command.productId(), date);

        eventHandledService.saveEventHandled(command.eventId(), DOMAIN_TYPE, command.metricsType().toString());

        // 랭킹
        rankingService.recordView(command.productId());

        return viewMetrics;
    }

}
