package com.loopers.domain.payment;


import java.util.List;

public interface PaymentRepository {

    Payment save(Payment payment);

    Payment findByTransactionKeyAndOrderId(String transactionKey, Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

}
