package com.loopers.application.payment;


import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentFailedEvent;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStrategy;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public PaymentInfo processPayment(ProcessPaymentCommand command) {
        // 결제 생성
        Payment payment = paymentService.createPayment(command);

        // 결제 전략 선택
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(command.paymentMethod());

        // 전략에 따라 결제 요청
        return strategy.pay(payment);
    }

    public void paymentCallback(CallbackPaymentCommand command) {
        paymentService.updateStatusFromCallback(command);
    }

    @Transactional
    public void paymentFailedEventHandler(PaymentFailedEvent event) {
        // 주문 상태 실패로 변경
        Order order = orderService.updateOrderStatusToFailed(event.orderId());

        // 상품 재고 원복
        order.getOrderItems().forEach(item -> {
            productService.restoreStock(item.getProduct().getId(), item.getQuantity());
        });

        // 쿠폰 사용 원복
        if (order.getCouponId() != null) {
            couponService.restoreUserCoupon(
                    order.getUser().getUserId(),
                    order.getCouponId()
            );
        }
    }
}
