package com.loopers.application.payment;


import com.loopers.domain.payment.CardType;

public record ProcessPaymentCommand(
        String userId,
        Long orderId,
        int amount,
        PaymentMethod paymentMethod,
        String cardNo,
        CardType cardType
) {
}
