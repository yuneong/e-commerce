package com.loopers.domain.ranking;

import com.loopers.application.metrics.MetricsCounter;
import com.loopers.support.generator.RedisKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingService {

    public final RankingRepository rankingRepository;

    public void processRanking(Map<Long, MetricsCounter> metricsCounters) {
        String key = RedisKeyGenerator.todayKey("ranking:all:");

        Map<Long, Double> scoreMap = new HashMap<>();

        metricsCounters.forEach((productId, metricsCounter) -> {
            double score = RankingCalculator.weightedSum(metricsCounter);
            scoreMap.put(productId, score);
        });

        // Redis ZSet 저장 (TypedTuple 사용)
        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();

        scoreMap.forEach((productId, score) -> {
            tuples.add(new DefaultTypedTuple<>(productId.toString(), score));
        });

        rankingRepository.saveAllScores(key, tuples);

        // TTL 설정
        rankingRepository.expire(key, Duration.ofDays(2));
    }

}
