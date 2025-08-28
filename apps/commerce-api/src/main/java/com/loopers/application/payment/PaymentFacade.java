package com.loopers.application.payment;


import com.loopers.domain.common.UserActionEnvelope;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStrategy;
import com.loopers.domain.payment.event.PaymentProcessEvent;
import com.loopers.domain.payment.event.PaymentSucceededEvent;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PaymentInfo processPayment(ProcessPaymentCommand command) {
        // 주문 검증
        orderService.getOrderById(command.orderId());

        // 결제 생성
        Payment payment = paymentService.createPayment(command);

        // 결제 전략 선택
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(command.paymentMethod());

        // event
        eventPublisher.publishEvent(UserActionEnvelope.of(
                "PAYMENT_PROCESS",
                PaymentProcessEvent.of(
                        command.orderId(),
                        payment.getId(),
                        command.amount(),
                        command.paymentMethod()
                )
        ));

        // 전략에 따라 결제 요청
        return strategy.pay(payment);
    }

    public void paymentCallback(CallbackPaymentCommand command) {
        paymentService.updateStatusFromCallback(command);
    }

    @Transactional
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        // 주문 상태 완료로 변경
        Order order = orderService.updateOrderStatus(event.orderId(), OrderStatus.PAID);

        // 쿠폰 사용 처리
        Long couponId = order.getCouponId();
        if (couponId != null) {
            couponService.useCoupon(event.userId(), couponId);
        }
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        // 주문 상태 실패로 변경
        Order order = orderService.updateOrderStatus(event.orderId(), OrderStatus.FAILED);

        // 상품 재고 원복
        order.getOrderItems().forEach(item -> {
            productService.restoreStock(item.getProduct().getId(), item.getQuantity());
        });
    }

}
