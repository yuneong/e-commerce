package com.loopers.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByTransactionKey(String transactionKey);

    Optional<Payment> findByTransactionKey(String userId, String transactionKey);

    List<Payment> findByOrderId(String userId, Long orderId);

}
