package com.loopers.domain.order.event;

import com.loopers.domain.order.OrderStatus;

public record OrderSucceededEvent(
        String userId,
        Long orderId,
        OrderStatus status
) {
}
