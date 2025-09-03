package com.loopers.application.metrics;

import com.loopers.domain.eventHandled.EventHandledDomainType;
import com.loopers.domain.eventHandled.EventHandledService;
import com.loopers.domain.metrics.ProductMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ProductMetricsFacade {

    private final ProductMetricsService productMetricsService;
    private final EventHandledService eventHandledService;
    private final static LocalDate today = LocalDate.now();
    private final static EventHandledDomainType DOMAIN_TYPE = EventHandledDomainType.METRICS;

    public void processLikeMetrics(ProductMetricsCommand command) {
        if (eventHandledService.isEventHandled(command.eventId())) {
            return;
        }

        productMetricsService.processLikeMetrics(command.productId(), command.likeType(), today);

        eventHandledService.saveEventHandled(command.eventId(), DOMAIN_TYPE, command.metricsType().toString());
    }

    public void processStockMetrics(ProductMetricsCommand command) {
        if (eventHandledService.isEventHandled(command.eventId())) {
            return;
        }

        productMetricsService.processStockMetrics(command.productId(), command.stock(), command.changedType(), today);

        eventHandledService.saveEventHandled(command.eventId(), DOMAIN_TYPE, command.metricsType().toString());
    }

    public void processViewMetrics(ProductMetricsCommand command) {
        if (eventHandledService.isEventHandled(command.eventId())) {
            return;
        }

        productMetricsService.processViewMetrics(command.productId(), today);

        eventHandledService.saveEventHandled(command.eventId(), DOMAIN_TYPE, command.metricsType().toString());
    }

}
