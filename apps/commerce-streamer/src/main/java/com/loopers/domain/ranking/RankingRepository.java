package com.loopers.domain.ranking;


import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.Set;

public interface RankingRepository {

    void increaseScore(String key, Long productId, double score);

    void expire(String key, Duration ttlSeconds);

    void saveAllScores(String key, Set<ZSetOperations.TypedTuple<String>> tuples);

}
