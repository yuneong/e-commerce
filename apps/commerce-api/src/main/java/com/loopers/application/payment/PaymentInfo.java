package com.loopers.application.payment;


import com.loopers.domain.payment.Payment;

public record PaymentInfo(
        Long paymentId,
        Long orderId,
        int amount,
        PaymentMethod paymentMethod
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getMethod()
        );
    }
}
