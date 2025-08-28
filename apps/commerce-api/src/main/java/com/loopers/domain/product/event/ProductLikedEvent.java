package com.loopers.domain.product.event;

import java.util.UUID;

public record ProductLikedEvent(
        String eventId,
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
                UUID.randomUUID().toString(),
                productId,
                userId,
                likeType
        );
    }

}
