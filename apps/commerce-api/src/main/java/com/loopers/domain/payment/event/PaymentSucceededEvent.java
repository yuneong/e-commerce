package com.loopers.domain.payment.event;

public record PaymentSucceededEvent(
        Long orderId,
        String userId
) {
}
