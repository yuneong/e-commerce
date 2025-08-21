package com.loopers.application.payment;


public record CallbackPaymentCommand(
        String transactionKey, // pgId
        Long orderId,
        String cardType,
        String cardNo,
        int amount,
        String status,
        String reason
) {

}
