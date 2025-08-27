package com.loopers.infrastructure.platform;

public record OrderResultMessage(
        String userId,
        Long orderId,
        Long paymentId
) {
    public OrderResultMessage(String userId, Long orderId) {
        this(userId, orderId, null);
    }
}
