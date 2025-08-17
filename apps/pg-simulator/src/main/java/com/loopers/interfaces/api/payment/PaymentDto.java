package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.OrderInfo;
import com.loopers.application.payment.PaymentCommand;
import com.loopers.application.payment.TransactionInfo;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.List;
import java.util.regex.Pattern;

public class PaymentDto {

    public record PaymentRequest(
            Long orderId,
            CardTypeDto cardType,
            String cardNo,
            Long amount,
            String callbackUrl
    ) {

        private static final Pattern REGEX_CARD_NO = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$");
        private static final String PREFIX_CALLBACK_URL = "http://localhost:8080";

        public void validate() {
//            if (orderId == null || orderId.isBlank() || orderId.length() < 6) {
//                throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 6자리 이상 문자열이어야 합니다.");
//            }
            if (!REGEX_CARD_NO.matcher(cardNo).matches()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "카드 번호는 xxxx-xxxx-xxxx-xxxx 형식이어야 합니다.");
            }
            if (amount == null || amount <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "결제금액은 양의 정수여야 합니다.");
            }
            if (callbackUrl == null || !callbackUrl.startsWith(PREFIX_CALLBACK_URL)) {
                throw new CoreException(ErrorType.BAD_REQUEST, "콜백 URL 은 " + PREFIX_CALLBACK_URL + " 로 시작해야 합니다.");
            }
        }

        public PaymentCommand toCommand(String userId) {
            return new PaymentCommand(
                    userId,
                    orderId,
                    cardType.toCardType(),
                    cardNo,
                    amount,
                    callbackUrl
            );
        }
    }

    public record TransactionDetailResponse(
            String transactionKey,
            Long orderId,
            CardTypeDto cardType,
            String cardNo,
            Long amount,
            TransactionStatusResponse status,
            String reason
    ) {
        public static TransactionDetailResponse from(TransactionInfo transactionInfo) {
            return new TransactionDetailResponse(
                    transactionInfo.transactionKey(),
                    transactionInfo.orderId(),
                    CardTypeDto.from(transactionInfo.cardType()),
                    transactionInfo.cardNo(),
                    transactionInfo.amount(),
                    TransactionStatusResponse.from(transactionInfo.status()),
                    transactionInfo.reason()
            );
        }
    }

    public record TransactionResponse(
            String transactionKey,
            TransactionStatusResponse status,
            String reason
    ) {
        public static TransactionResponse from(TransactionInfo transactionInfo) {
            return new TransactionResponse(
                    transactionInfo.transactionKey(),
                    TransactionStatusResponse.from(transactionInfo.status()),
                    transactionInfo.reason()
            );
        }
    }

    public record OrderResponse(
            Long orderId,
            List<TransactionResponse> transactions
    ) {
        public static OrderResponse from(OrderInfo orderInfo) {
            return new OrderResponse(
                    orderInfo.orderId(),
                    orderInfo.transactions().stream()
                            .map(TransactionResponse::from)
                            .toList()
            );
        }
    }

    public enum CardTypeDto {
        SAMSUNG,
        KB,
        HYUNDAI;

        public CardType toCardType() {
            return switch (this) {
                case SAMSUNG -> CardType.SAMSUNG;
                case KB -> CardType.KB;
                case HYUNDAI -> CardType.HYUNDAI;
            };
        }

        public static CardTypeDto from(CardType cardType) {
            return switch (cardType) {
                case SAMSUNG -> SAMSUNG;
                case KB -> KB;
                case HYUNDAI -> HYUNDAI;
            };
        }
    }

    public enum TransactionStatusResponse {
        PENDING,
        SUCCESS,
        FAILED;

        public static TransactionStatusResponse from(TransactionStatus transactionStatus) {
            return switch (transactionStatus) {
                case PENDING -> PENDING;
                case SUCCESS -> SUCCESS;
                case FAILED -> FAILED;
            };
        }
    }

}
