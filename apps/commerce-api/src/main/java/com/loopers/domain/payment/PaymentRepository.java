package com.loopers.domain.payment;


import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Payment findByTransactionKeyAndOrderId(String transactionKey, Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findById(Long paymentId);

}
