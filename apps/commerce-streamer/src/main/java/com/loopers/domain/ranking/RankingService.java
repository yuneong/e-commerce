package com.loopers.domain.ranking;

import com.loopers.application.metrics.MetricsCounter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingService {

    public final RankingRepository rankingRepository;
    public final RankingScorePolicy scorePolicy;
    public static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    public String buildKey(LocalDate date) {
        return "ranking:all:" + date.format(DAY);
    }

    public String todayKey() {
        return buildKey(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }

    public void recordLike(Long productId) {
        double score = scorePolicy.scoreFor(RankingEventType.LIKE, RankingScorePolicy.RankingContext.empty());
        rankingRepository.increaseScore(todayKey(), productId, score);
    }

    public void recordOrder(Long productId, int amount, int price) {
        double score = scorePolicy.scoreFor(RankingEventType.ORDER, RankingScorePolicy.RankingContext.order(price, amount));
        rankingRepository.increaseScore(todayKey(), productId, score);
    }

    public void recordView(Long productId) {
        double score = scorePolicy.scoreFor(RankingEventType.VIEW, RankingScorePolicy.RankingContext.empty());
        rankingRepository.increaseScore(todayKey(), productId, score);
    }

    public void processRanking(Map<Long, MetricsCounter> metricsCounters) {
        String key = todayKey();

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
        rankingRepository.expire(key, 2 * 24 * 60 * 60); // 2일
    }

}
