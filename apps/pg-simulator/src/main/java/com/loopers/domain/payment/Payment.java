package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_user_transaction", columnList = "user_id, transaction_key"),
        @Index(name = "idx_user_order", columnList = "user_id, order_id"),
        @Index(name = "idx_unique_user_order_transaction", columnList = "user_id, order_id, transaction_key", unique = true)
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @Column(name = "transaction_key", nullable = false, unique = true)
    private String transactionKey;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Column(name = "card_no", nullable = false)
    private String cardNo;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "reason", nullable = true)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static Payment create(
        String transactionKey,
        String userId,
        Long orderId,
        CardType cardType,
        String cardNo,
        Long amount,
        String callbackUrl
    ) {
        Payment payment = new Payment();

        payment.transactionKey = transactionKey;
        payment.userId = userId;
        payment.orderId = orderId;
        payment.cardType = cardType;
        payment.cardNo = cardNo;
        payment.amount = amount;
        payment.callbackUrl = callbackUrl;

        return payment;
    }

    public void approve() {
        if (status != TransactionStatus.PENDING) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "결제승인은 대기상태에서만 가능합니다.");
        }
        status = TransactionStatus.SUCCESS;
        reason = "정상 승인되었습니다.";
    }

    public void invalidCard() {
        if (status != TransactionStatus.PENDING) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "결제처리는 대기상태에서만 가능합니다.");
        }
        status = TransactionStatus.FAILED;
        reason = "잘못된 카드입니다. 다른 카드를 선택해주세요.";
    }

    public void limitExceeded() {
        if (status != TransactionStatus.PENDING) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "한도초과 처리는 대기상태에서만 가능합니다.");
        }
        status = TransactionStatus.FAILED;
        reason = "한도초과입니다. 다른 카드를 선택해주세요.";
    }

}
