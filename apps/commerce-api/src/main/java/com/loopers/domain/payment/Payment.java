package com.loopers.domain.payment;

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

    String transactionKey; // pgId

    private CardType cardType;

    private String cardNo;

    private Long amount;

    private PaymentStatus status;

    private String reason;


    public static Payment create(CreatePaymentDto dto) {
        Payment payment = new Payment();

        payment.userId = dto.userId();
        payment.orderId = dto.orderId();
        payment.cardType = dto.cardType();
        payment.cardNo = dto.cardNo();
        payment.amount = dto.amount();
        payment.status = PaymentStatus.PENDING;

        return payment;
    }

    public Payment updateTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;

        return this;
    }

    public void updateStatus(PaymentStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

}
