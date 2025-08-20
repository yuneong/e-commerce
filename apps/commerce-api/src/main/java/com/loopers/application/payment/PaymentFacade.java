package com.loopers.application.payment;


import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;
    private final PaymentStrategyFactory paymentStrategyFactory;

    @Transactional
    public PaymentInfo processPayment(ProcessPaymentCommand command) {
        // 결제 생성
        Payment payment = paymentService.createPayment(command);

        // 결제 전략 선택
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(command.paymentMethod());

        // 전략에 따라 결제 요청
        return strategy.pay(payment);
    }

    public void paymentCallback(PaymentCommand command) {
        paymentService.updatePaymentStatus(command);
    }

}
