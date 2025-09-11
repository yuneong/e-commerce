package com.loopers.domain.ranking;

import com.loopers.support.generator.RedisKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;

    public List<Ranking> getRankings(LocalDate date, Pageable page) {
        // key
        String key = RedisKeyGenerator.buildKey("ranking:all:", date);

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, page.getOffset(), page.getPageSize());

        return tuples.stream()
                .map(t -> Ranking.create(
                        Long.valueOf(t.getValue()),
                        t.getScore()
                ))
                .toList();
    }

    public void carryOverYesterdayRankings(LocalDate from, LocalDate to, double weight) {
        String fromKey = RedisKeyGenerator.buildKey("ranking:all:", from);
        String toKey = RedisKeyGenerator.buildKey("ranking:all:", to);

        // 어제 랭킹
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().rangeWithScores(fromKey, 0, -1);

        if (tuples == null || tuples.isEmpty()) {
            return;
        }

        // 가중치 적용하여 오늘 랭킹에 추가
        tuples.forEach(t -> {
            double newScore = t.getScore() * weight;
            redisTemplate.opsForZSet().add(toKey, t.getValue(), newScore);
        });

        redisTemplate.expire(toKey, Duration.ofDays(2));
    }

}
