package com.loopers.domain.product.event;

import java.util.List;
import java.util.UUID;

public record ProductViewedEvent(
        String eventId,
        List<Long> productId
) {

    public static ProductViewedEvent of(
            List<Long> productId
    ) {
        return new ProductViewedEvent(
                UUID.randomUUID().toString(),
                productId
        );
    }

    public static ProductViewedEvent of(
            Long productId
    ) {
        return new ProductViewedEvent(
                UUID.randomUUID().toString(),
                List.of(productId)
        );
    }

}
