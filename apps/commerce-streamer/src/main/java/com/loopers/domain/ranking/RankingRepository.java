package com.loopers.domain.ranking;


public interface RankingRepository {

    void increaseScore(String key, Long productId, double score);

    void expire(String key, long ttlSeconds);

}
