package com.loopers.application.payment;


import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;

    public void paymentCallback(PaymentCommand command) {
        paymentService.updatePaymentStatus(command);
    }

}
