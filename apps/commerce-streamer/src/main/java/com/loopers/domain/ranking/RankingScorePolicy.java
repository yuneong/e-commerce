package com.loopers.domain.ranking;

public interface RankingScorePolicy {

    double scoreFor(RankingEventType type);

}
