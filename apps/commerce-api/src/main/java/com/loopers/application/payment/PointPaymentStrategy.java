package com.loopers.application.payment;


import com.loopers.domain.payment.*;
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
        } catch (CoreException e) {
            // 실패 시
            payment.updateStatus(PaymentStatus.FAILED, e.getMessage());
        } finally {
            savedPayment = paymentService.savePayment(payment);
        }

        return PaymentInfo.from(savedPayment);
    }



















}
