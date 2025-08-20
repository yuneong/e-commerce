package com.loopers.application.payment;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentFailedEvent;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentFailedEventHandler {

    private final ProductService productService;
    private final CouponService couponService;
    private final OrderService orderService;

    /**
     * 결제 실패 이벤트 처리
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
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
