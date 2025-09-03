package com.loopers.domain.product.event;


public record ProductLikedEvent(
        Long productId,
        String userId,
        String likeType
) {

    public static ProductLikedEvent of(
            Long productId,
            String userId,
            String likeType
    ) {
        return new ProductLikedEvent(
                productId,
                userId,
                likeType
        );
    }

}
