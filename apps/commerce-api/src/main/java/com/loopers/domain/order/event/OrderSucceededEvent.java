package com.loopers.domain.order.event;


public record OrderSucceededEvent(
        String userId,
        Long orderId
) {
}
