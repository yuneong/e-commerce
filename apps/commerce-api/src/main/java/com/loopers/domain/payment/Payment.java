package com.loopers.domain.payment;

import com.loopers.application.payment.PaymentMethod;
import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

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

        payment.validate();

        return payment;
    }

    public static Payment createPointPayment(String userId, Long orderId, int amount) {
        Payment payment = new Payment();

        payment.userId = userId;
        payment.orderId = orderId;
        payment.amount = amount;
        payment.method = PaymentMethod.POINT;
        payment.status = PaymentStatus.PENDING;

        payment.validate();

        return payment;
    }

    public void updateStatus(PaymentStatus status, String reason) {
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 상태는 필수값입니다.");
        }

        this.status = status;
        this.reason = reason;
    }

    public void updateTransactionKey(String transactionKey) {
        if (this.method != PaymentMethod.CARD) {
            throw new CoreException(ErrorType.BAD_REQUEST, "거래 키는 카드 결제에만 적용 가능합니다.");
        }
        if (transactionKey == null || transactionKey.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "거래 키는 공백일 수 없습니다.");
        }
        this.cardDetail = this.cardDetail.withTransactionKey(transactionKey);
    }


    private static final Pattern REGEX_CARD_NO = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$");

    public void validate() {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 ID는 필수값입니다.");
        }
        if (orderId == null || orderId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 양수여야 합니다.");
        }
        if (amount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제금액은 0 이상이어야 합니다.");
        }

        if (method == PaymentMethod.CARD) {
            if (cardDetail == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "카드 결제에는 카드 정보가 필요합니다.");
            }
            if (cardDetail.getCardNo() == null || !REGEX_CARD_NO.matcher(cardDetail.getCardNo()).matches()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "카드 번호는 xxxx-xxxx-xxxx-xxxx 형식이어야 합니다.");
            }
            if (cardDetail.getCardType() == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "카드 타입은 필수값입니다.");
            }
        }
    }

}
