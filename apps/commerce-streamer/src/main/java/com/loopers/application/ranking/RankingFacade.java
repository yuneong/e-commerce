package com.loopers.application.ranking;

import com.loopers.application.metrics.MetricsCounter;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RankingFacade {

    private final RankingService rankingService;

    public void processRanking(Map<Long, MetricsCounter> metricsCounters) {
        rankingService.processRanking(metricsCounters);
    }

}
