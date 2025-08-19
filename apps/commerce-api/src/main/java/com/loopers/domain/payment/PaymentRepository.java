package com.loopers.domain.payment;


public interface PaymentRepository {

    Payment save(Payment payment);

    Payment findByTransactionKeyAndOrderId(String transactionKey, Long orderId);

}
