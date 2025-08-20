package com.loopers.domain.payment;

import com.loopers.application.payment.PaymentMethod;
import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    private String userId;
    private Long orderId;
    private int amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String reason;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cardNo", column = @Column(name = "card_no")),
            @AttributeOverride(name = "cardType", column = @Column(name = "card_type")),
            @AttributeOverride(name = "transactionKey", column = @Column(name = "transaction_key"))
    })
    private CardDetail cardDetail; // 카드 정보

    public static Payment createCardPayment(String userId, Long orderId, int amount, CardDetail cardDetail) {
        Payment payment = new Payment();

        payment.userId = userId;
        payment.orderId = orderId;
        payment.amount = amount;
        payment.method = PaymentMethod.CARD;
        payment.cardDetail = cardDetail;
        payment.status = PaymentStatus.PENDING;

        return payment;
    }

    public static Payment createPointPayment(String userId, Long orderId, int amount) {
        Payment payment = new Payment();

        payment.userId = userId;
        payment.orderId = orderId;
        payment.amount = amount;
        payment.method = PaymentMethod.POINT;
        payment.status = PaymentStatus.PENDING;

        return payment;
    }

    public void updateStatus(PaymentStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public void updateTransactionKey(String transactionKey) {
        if (this.cardDetail != null) {
            this.cardDetail = this.cardDetail.withTransactionKey(transactionKey);
        }
    }

}
