package com.loopers.domain.payment;

import com.loopers.application.payment.CallbackPaymentCommand;
import com.loopers.application.payment.ProcessPaymentCommand;
import com.loopers.infrastructure.pg.PgClient;
import com.loopers.infrastructure.pg.PgV1Dto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PgClient pgClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Payment createPayment(ProcessPaymentCommand command) {
        Payment payment = switch (command.paymentMethod()) {
            case CARD -> Payment.createCardPayment(
                    command.userId(),
                    command.orderId(),
                    command.amount(),
                    new CardDetail(
                            command.cardNo(),
                            command.cardType(),
                            null // 최초 생성 시 transactionKey 없음
                    )
                );
            case POINT -> Payment.createPointPayment(
                        command.userId(),
                        command.orderId(),
                        command.amount()
                );
        };

        return paymentRepository.save(payment);
    }

    @CircuitBreaker(name = "pgCircuit", fallbackMethod = "requestAndSavePaymentFallback")
    @Retry(name = "pgRetry")
    public Payment requestAndSavePayment(Payment payment, String callbackUrl) {
        PgV1Dto.PgRequest request = PgV1Dto.PgRequest.from(payment, callbackUrl);
        PgV1Dto.PgResponse response = pgClient.callPayment(payment.getUserId(), request);

        payment.updateTransactionKey(response.transactionKey());

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment requestAndSavePaymentFallback(Payment payment, Throwable t) {
        payment.updateStatus(PaymentStatus.FAILED, t.getMessage());
        eventPublisher.publishEvent(new PaymentFailedEvent(
                payment.getOrderId(),
                payment.getUserId()
        ));

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment updatePaymentStatus(PaymentCommand command) {
        Payment payment = paymentRepository.findByTransactionKeyAndOrderId(command.transactionKey(), command.orderId());

        payment.updateStatus(PaymentStatus.valueOf(command.status()), command.reason());

        paymentRepository.save(payment);

        return payment;
    }

    @Transactional
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}
