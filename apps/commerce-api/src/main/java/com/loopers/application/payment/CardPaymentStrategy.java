package com.loopers.application.payment;


import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardPaymentStrategy implements PaymentStrategy {

    private final PaymentService paymentService;
    @Value("${payment.callback.url}") private String CALLBACK_URL;

    @Override
    public PaymentInfo pay(Payment payment) {
        // 결제 요청
        Payment savePayment = paymentService.requestAndSavePayment(payment, CALLBACK_URL);

        return PaymentInfo.from(savePayment);
    }

}
