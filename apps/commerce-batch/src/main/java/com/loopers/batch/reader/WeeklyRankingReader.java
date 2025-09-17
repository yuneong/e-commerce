package com.loopers.batch.reader;

import com.loopers.domain.metrics.ProductMetricsRepository;
import com.loopers.dto.ProductMetricsSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
public class WeeklyRankingReader implements ItemReader<ProductMetricsSummary> {

    private final ProductMetricsRepository productMetricsRepository;

    private Iterator<ProductMetricsSummary> iterator;

    @Value("#{jobParameters['startDate']}")
    private String startDate;

    @Value("#{jobParameters['endDate']}")
    private String endDate;

    @Override
    public ProductMetricsSummary read() {
        if (iterator == null) {
            LocalDate parsedStartDate = LocalDate.parse(startDate);
            LocalDate parsedEndDate = LocalDate.parse(endDate);

            List<ProductMetricsSummary> summaries = productMetricsRepository.findByIdMetricsDateBetween(parsedStartDate, parsedEndDate);

            iterator = summaries.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }

}
