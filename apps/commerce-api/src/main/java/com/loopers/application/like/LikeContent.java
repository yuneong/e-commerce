package com.loopers.application.like;

import com.loopers.domain.like.LikeStatus;
import com.loopers.domain.product.Product;

public record LikeContent(
        Product product,
        Long likeCount,
        LikeStatus likedYn
) {

    public static LikeContent from(Product product, Long likeCount, LikeStatus likedYn) {
        return new LikeContent(
                product,
                likeCount,
                likedYn
        );
    }

}
