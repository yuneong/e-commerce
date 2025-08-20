package com.loopers.domain.payment;

public record PaymentFailedEvent(
        Long orderId,
        String userId
) {
}
