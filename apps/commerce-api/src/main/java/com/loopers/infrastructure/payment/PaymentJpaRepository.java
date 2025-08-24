package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Payment findByCardDetailTransactionKeyAndOrderId(String transactionKey, Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

}
