package com.loopers.domain.ranking;

public interface RankingScorePolicy {

    double scoreFor(RankingEventType type, RankingContext context);

    record RankingContext(int price, int amount) {
        public static RankingContext empty() {
            return new RankingContext(0, 0);
        }

        public static RankingContext order(int price, int amount) {
            return new RankingContext(price, amount);
        }
    }

}
