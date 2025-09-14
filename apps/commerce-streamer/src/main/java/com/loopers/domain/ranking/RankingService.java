package com.loopers.domain.ranking;

import com.loopers.application.metrics.MetricsCounter;
import com.loopers.support.generator.RedisKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    private final RankingCalculator rankingCalculator;
    private final RedisTemplate<String, String> redisTemplate;

    public void processRanking(Map<Long, MetricsCounter> metricsCounters) {
        if (metricsCounters == null || metricsCounters.isEmpty()) {
            return;
        }

        String key = RedisKeyGenerator.todayKey("ranking:all:");

        Map<Long, Double> scoreMap = new HashMap<>();

        metricsCounters.forEach((productId, metricsCounter) -> {
            double score = rankingCalculator.weightedSum(metricsCounter);
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

    public void carryOverYesterdayRankings(LocalDate from, LocalDate to, double weight) {
        String fromKey = RedisKeyGenerator.buildKey("ranking:all:", from);
        String toKey = RedisKeyGenerator.buildKey("ranking:all:", to);

        // 오늘 랭킹
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().rangeWithScores(fromKey, 0, -1);

        if (tuples == null || tuples.isEmpty()) {
            return;
        }

        // 가중치 적용하여 내일 랭킹에 추가
        tuples.forEach(t -> {
            double newScore = t.getScore() * weight;
            redisTemplate.opsForZSet().add(toKey, t.getValue(), newScore);
        });

        redisTemplate.expire(toKey, Duration.ofDays(2));
    }

}
