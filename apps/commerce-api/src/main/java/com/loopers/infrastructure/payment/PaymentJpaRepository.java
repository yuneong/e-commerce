package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Payment findByTransactionKeyAndOrderId(String transactionKey, Long orderId);

}
