package com.loopers.interfaces.listener;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentFacade paymentFacade;

    /**
     * 결제 성공 이벤트 처리
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePaymentSucceededEvent(PaymentSucceededEvent event) {
        paymentFacade.handlePaymentSucceeded(event);
    }

    /**
     * 결제 실패 이벤트 처리
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        paymentFacade.handlePaymentFailed(event);
    }

}
