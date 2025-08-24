package com.loopers.domain.product;

import java.util.UUID;

public record ProductLikeEvent(
        String eventId,
        Long productId,
        String userId,
        String likeType
) {

    public static ProductLikeEvent of(
            Long productId,
            String userId,
            String likeType
    ) {
        return new ProductLikeEvent(
                UUID.randomUUID().toString(),
                productId,
                userId,
                likeType
        );
    }
}
