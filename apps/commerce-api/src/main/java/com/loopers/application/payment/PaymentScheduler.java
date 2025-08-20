package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;

    /**
     * pg사 결제 상태를 주기적으로 확인하는 스케줄러
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void updatePaymentsStatus() {
        // 결제 상태가 PENDING인 결제들을 조회
        // 각 결제 상태를 조회하고 갱신
        paymentService.updatePaymentStatusWithScheduler();
    }

}
