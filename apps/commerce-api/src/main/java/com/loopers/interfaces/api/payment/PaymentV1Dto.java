package com.loopers.interfaces.api.payment;


import com.loopers.application.payment.*;
import com.loopers.domain.payment.CardType;

public class PaymentV1Dto {

    public record ProcessRequest(
            String userId,
            Long orderId,
            int amount,
            PaymentMethod paymentMethod,
            String cardNo,
            String cardType
    ) {
        public static ProcessPaymentCommand toCommand(ProcessRequest request) {
            return new ProcessPaymentCommand(
                    request.userId,
                    request.orderId,
                    request.amount,
                    request.paymentMethod,
                    request.cardNo,
                    CardType.valueOf(request.cardType)
            );
        }
    }

    public record ProcessResponse(
            Long paymentId,
            Long orderId,
            int amount,
            PaymentMethod paymentMethod
    ) {
        public static ProcessResponse from(PaymentInfo paymentInfo) {
            return new ProcessResponse(
                    paymentInfo.paymentId(),
                    paymentInfo.orderId(),
                    paymentInfo.amount(),
                    paymentInfo.paymentMethod()
            );
        }
    }

    public record CallbackRequest(
            String transactionKey, // pgId
            Long orderId,
            String cardType,
            String cardNo,
            Long amount,
            String status,
            String reason
    ) {
        public static CallbackPaymentCommand toCommand(CallbackRequest request) {
            return new CallbackPaymentCommand(
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
