package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class PaymentCoreRepository implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentCoreRepository(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Transactional
    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Payment> findByTransactionKey(String transactionKey) {
        return paymentJpaRepository.findById(transactionKey);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Payment> findByTransactionKey(String userId, String transactionKey) {
        return paymentJpaRepository.findByUserIdAndTransactionKey(userId, transactionKey);
    }

    @Override
    public List<Payment> findByOrderId(String userId, Long orderId) {
        List<Payment> payments = paymentJpaRepository.findByUserIdAndOrderId(userId, orderId);
        payments.sort(Comparator.comparing(Payment::getUpdatedAt).reversed());
        return payments;
    }
}
