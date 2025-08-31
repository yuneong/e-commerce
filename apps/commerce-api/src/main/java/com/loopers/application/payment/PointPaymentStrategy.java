package com.loopers.application.payment;


import com.loopers.domain.payment.*;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.event.PaymentSucceededEvent;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointPaymentStrategy implements PaymentStrategy {

    private final UserService userService;
    private final PointService pointService;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PaymentInfo pay(Payment payment) {
        // 조회
        User user = userService.getMyInfo(payment.getUserId());
        Point point = pointService.getPointWithLock(user);
        Payment savedPayment = null;

        try {
            // 포인트 차감
            pointService.checkAndUsePoint(point, payment.getAmount());

            // 성공 시
            payment.updateStatus(PaymentStatus.SUCCESS, null);
            eventPublisher.publishEvent(new PaymentSucceededEvent(
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getId()
            ));
        } catch (Exception e) {
            // 실패 시
            payment.updateStatus(PaymentStatus.FAILED, e.getMessage());
            eventPublisher.publishEvent(new PaymentFailedEvent(
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getId()
            ));
        } finally {
            savedPayment = paymentService.savePayment(payment);
        }

        return PaymentInfo.from(savedPayment);
    }
    
}
