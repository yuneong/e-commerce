package com.loopers.application.payment;

import com.loopers.domain.payment.CardType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;


public record PaymentCommand(
    String userId,
    Long orderId,
    CardType cardType,
    String cardNo,
    Long amount,
    String callBackUrl
) {

    public void validate() {
        if (amount <= 0L) {
            throw new CoreException(ErrorType.BAD_REQUEST, "요청 금액은 0 보다 큰 정수여야 합니다.");
        }
    }

}
