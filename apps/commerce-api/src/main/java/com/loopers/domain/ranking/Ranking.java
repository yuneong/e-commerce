package com.loopers.domain.ranking;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ranking {

    private Long productId;
    private Double score;

    public static Ranking create(Long productId, Double score) {
        Ranking ranking = new Ranking();

        ranking.productId = productId;
        ranking.score = score;
        return ranking;

    }
}
