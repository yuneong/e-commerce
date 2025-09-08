package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RedisRankingRepositoryImpl implements RankingRepository {

    public StringRedisTemplate redisTemplate;

    @Override
    public void increaseScore(String key, Long productId, double score) {
        redisTemplate.opsForZSet().incrementScore(key, productId.toString(), score);
    }

    @Override
    public void expire(String key, long ttlSeconds) {
        redisTemplate.expire(key, java.time.Duration.ofSeconds(ttlSeconds));
    }
}
