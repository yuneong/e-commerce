package com.loopers.interfaces.api.payment;


import com.loopers.application.payment.PaymentCommand;

public class PaymentV1Dto {

    public record PaymentRequest(
            String transactionKey, // pgId
            Long orderId,
            String cardType,
            String cardNo,
            Long amount,
            String status,
            String reason
    ) {

        public static PaymentCommand toCommand(PaymentRequest request) {
            return new PaymentCommand(
                    request.transactionKey,
                    request.orderId,
                    request.cardType,
                    request.cardNo,
                    request.amount,
                    request.status,
                    request.reason
            );
        }

    }

}
