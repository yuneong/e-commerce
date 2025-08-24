package com.loopers.interfaces.event.payment;

import com.loopers.application.payment.PaymentApplicationService;
import com.loopers.domain.payment.PaymentEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class PaymentEventListener {

    private final PaymentApplicationService paymentApplicationService;

    public PaymentEventListener(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentEvent.PaymentCreated event) {
        long thresholdMillis = ThreadLocalRandom.current().nextLong(1000L, 5001L);
        try {
            Thread.sleep(thresholdMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        paymentApplicationService.handle(event.transactionKey());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentEvent.PaymentHandled event) {
        paymentApplicationService.notifyTransactionResult(event.transactionKey());
    }

}
