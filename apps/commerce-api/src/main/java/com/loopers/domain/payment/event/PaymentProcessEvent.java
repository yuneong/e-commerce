package com.loopers.domain.payment.event;


import com.loopers.domain.payment.PaymentMethod;

public record PaymentProcessEvent(
        Long orderId,
        Long paymentId,
        int totalPrice,
        PaymentMethod paymentMethod
) {

    public static PaymentProcessEvent of (
            Long orderId,
            Long paymentId,
            int totalPrice,
            PaymentMethod paymentMethod
    ) {
        return new PaymentProcessEvent(
                orderId,
                paymentId,
                totalPrice,
                paymentMethod
        );
    }

}
