package com.loopers.application.payment;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.TransactionStatus;

/**
 * 트랜잭션 정보
 *
 * @property transactionKey 트랜잭션 KEY
 * @property orderId 주문 ID
 * @property cardType 카드 종류
 * @property cardNo 카드 번호
 * @property amount 금액
 * @property status 처리 상태
 * @property reason 처리 사유
 */
public record TransactionInfo(
    String transactionKey,
    Long orderId,
    CardType cardType,
    String cardNo,
    Long amount,
    TransactionStatus status,
    String reason
) {
    public static TransactionInfo from(Payment payment) {
        return new TransactionInfo(
                payment.getTransactionKey(),
                payment.getOrderId(),
                payment.getCardType(),
                payment.getCardNo(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getReason()
        );
    }
}
