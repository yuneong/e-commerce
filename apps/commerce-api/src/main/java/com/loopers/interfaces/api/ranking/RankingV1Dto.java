package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingInfo;

import java.util.List;

public class RankingV1Dto {

    public record RankingsResponse(
            Long productId,
            String productName,
            String imageUrl,
            int price,
            Long likeCount,
            String brandName,
            int rank
    ) {

        public static List<RankingsResponse> from(List<RankingInfo> infos) {
            return infos.stream()
                    .map(info -> new RankingsResponse(
                            info.productId(),
                            info.productName(),
                            info.imageUrl(),
                            info.price(),
                            info.likeCount(),
                            info.brandName(),
                            info.rank()
                    ))
                    .toList();
        }

    }

}
