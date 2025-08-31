package com.loopers.application.payment;


import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentStatus;

public record PaymentInfo(
        Long paymentId,
        Long orderId,
        int amount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String reason
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getReason()
        );
    }
}
