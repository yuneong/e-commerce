package com.loopers.infrastructure.pg;

import com.loopers.domain.payment.Payment;

public record PgRequest(
        Long orderId,
        String cardType,
        String cardNo,
        Long amount,
        String callbackUrl
) {

    public static PgRequest from(Payment payment, String callbackUrl) {
        return new PgRequest(
                payment.getOrderId(),
                payment.getCardType().toString(),
                payment.getCardNo(),
                payment.getAmount(),
                callbackUrl
        );
    }

}
