package com.loopers.application.payment;


import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentMethod;

public record ProcessPaymentCommand(
        String userId,
        Long orderId,
        int amount,
        PaymentMethod paymentMethod,
        String cardNo,
        CardType cardType
) {
}
