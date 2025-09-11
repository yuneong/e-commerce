package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class RedisRankingRepositoryImpl implements RankingRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void increaseScore(String key, Long productId, double score) {
        redisTemplate.opsForZSet().incrementScore(key, productId.toString(), score);
    }

    @Override
    public void expire(String key, Duration ttlSeconds) {
        redisTemplate.expire(key, ttlSeconds);
    }

    @Override
    public void saveAllScores(String key, Set<ZSetOperations.TypedTuple<String>> tuples) {
        redisTemplate.opsForZSet().add(key, tuples);
    }

}
