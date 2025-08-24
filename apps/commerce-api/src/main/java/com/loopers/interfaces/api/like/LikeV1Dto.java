package com.loopers.interfaces.api.like;


import com.loopers.application.like.LikeContent;
import com.loopers.application.like.LikeInfo;
import com.loopers.application.like.LikeListInfo;
import com.loopers.domain.like.LikeStatus;
import com.loopers.domain.product.Product;

import java.util.List;

public class LikeV1Dto {

    public record LikeResponse(
            LikeStatus likedYn
    ) {
        public static LikeResponse from(LikeInfo info) {
            return new LikeResponse(
                    info.likedYn()
            );
        }
    }

    public record LikeContentResponse(
            Long productId,
            String productName,
            String productImageUrl,
            int productPrice,
            Long likeCount,
            LikeStatus likedYn
    ) {
        public static LikeContentResponse from(LikeContent content) {
            Product product = content.product();
            return new LikeContentResponse(
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    product.getPrice(),
                    content.likeCount(),
                    content.likedYn()
            );
        }
    }

    public record LikeListResponse(
            List<LikeContentResponse> contents
    ) {
        public static LikeListResponse from(LikeListInfo info) {
            return new LikeListResponse(
                    info.contents().stream()
                            .map(LikeContentResponse::from)
                            .toList()
            );
        }
    }

}
