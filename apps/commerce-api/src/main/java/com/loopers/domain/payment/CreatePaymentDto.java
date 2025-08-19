package com.loopers.domain.payment;

public record CreatePaymentDto(
        String userId,
        Long orderId,
        CardType cardType,
        String cardNo,
        Long amount,
        String callbackUrl
) {

    public static CreatePaymentDto of(
            String userId,
            Long orderId,
            CardType cardType,
            String cardNo,
            Long amount,
            String callbackUrl
    ) {
        return new CreatePaymentDto(
                userId,
                orderId,
                cardType,
                cardNo,
                amount,
                callbackUrl
        );
    }
}
