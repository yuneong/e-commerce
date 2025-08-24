package com.loopers.domain.payment;

public final class PaymentEvent {

    private PaymentEvent() {
        // 인스턴스화 방지
    }

    public record PaymentCreated(String transactionKey) {
        public static PaymentCreated from(Payment payment) {
            return new PaymentCreated(payment.getTransactionKey());
        }
    }

    public record PaymentHandled(
            String transactionKey,
            TransactionStatus status,
            String reason,
            String callbackUrl
    ) {
        public static PaymentHandled from(Payment payment) {
            return new PaymentHandled(
                    payment.getTransactionKey(),
                    payment.getStatus(),
                    payment.getReason(),
                    payment.getCallbackUrl()
            );
        }
    }

}
