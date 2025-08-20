package com.loopers.application.payment;


public record CallbackPaymentCommand(
        String transactionKey, // pgId
        Long orderId,
        String cardType,
        String cardNo,
        Long amount,
        String status,
        String reason
) {

}
