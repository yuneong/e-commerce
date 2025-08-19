package com.loopers.domain.payment;

import com.loopers.application.payment.PaymentCommand;
import com.loopers.infrastructure.pg.PgClient;
import com.loopers.infrastructure.pg.PgRequest;
import com.loopers.infrastructure.pg.PgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PgClient pgClient;

    @Transactional
    public Payment createPayment(CreatePaymentDto dto) {
        Payment payment = Payment.create(dto);
        return paymentRepository.save(payment);
    }

    public Payment requestAndSavePayment(Payment payment, String callbackUrl) {
        PgRequest request = PgRequest.from(payment, callbackUrl);
        PgResponse response = pgClient.callPayment(payment.getUserId(), request);

        Payment updatedPayment = payment.updateTransactionKey(response.transactionKey());

        return paymentRepository.save(updatedPayment);
    }

    @Transactional
    public Payment updatePaymentStatus(PaymentCommand command) {
        Payment payment = paymentRepository.findByTransactionKeyAndOrderId(command.transactionKey(), command.orderId());

        payment.updateStatus(PaymentStatus.valueOf(command.status()), command.reason());

        paymentRepository.save(payment);

        return payment;
    }

}
