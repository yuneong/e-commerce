package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final PointPaymentStrategy pointStrategy;
    private final CardPaymentStrategy cardStrategy;

    public PaymentStrategy getStrategy(PaymentMethod method) {
        return switch (method) {
            case POINT -> pointStrategy;
            case CARD -> cardStrategy;
        };
    }

}
