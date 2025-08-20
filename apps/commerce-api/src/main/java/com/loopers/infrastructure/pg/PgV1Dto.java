package com.loopers.infrastructure.pg;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentStatus;

public class PgV1Dto {

    public record PgRequest(
            Long orderId,
            String cardType,
            String cardNo,
            int amount,
            String callbackUrl
    ) {
        public static PgRequest from(Payment payment, String callbackUrl) {
            return new PgRequest(
                    payment.getOrderId(),
                    payment.getCardDetail().getCardType().toString(),
                    payment.getCardDetail().getCardNo(),
                    payment.getAmount(),
                    callbackUrl
            );
        }
    }

    public record PgResponse(
            String transactionKey,
            String status,
            String reason
    ) {
    }

}
