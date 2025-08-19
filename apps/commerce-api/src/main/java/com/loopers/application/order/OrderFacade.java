package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.DiscountedOrderByCoupon;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.CreatePaymentDto;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;


@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final PointService pointService;
    private final CouponService couponService;
    private final PaymentService paymentService;
    @Value("${payment.callback.url}") public static String CALLBACK_URL;

    @Transactional
    public OrderInfo placeOrder(OrderCommand command) {
        // 유저, 포인트 조회
        User user = userService.getMyInfo(command.userId());
        Point point = pointService.getPointWithLock(user);

        // 상품 조회
        List<Product> products = productService.getProductsByIdsWithLock(command.items()
                .stream().map(OrderItemCommand::productId).toList());
        List<OrderItem> items = OrderItemFactory.createFrom(command.items(), products);

        // 쿠폰 조회 및 적용, 사용
        DiscountedOrderByCoupon discountedOrderByCoupon = couponService.useCoupon(command.userId(), command.couponId(), items);

        // 주문 생성
        Order order = orderService.createOrder(user, items, discountedOrderByCoupon);

        // 상품 재고 차감 (검증 포함)
        productService.checkAndDecreaseStock(order.getOrderItems());

        // 포인트 차감 (검증 포함)
        pointService.checkAndUsePoint(point, discountedOrderByCoupon.discountedTotalPrice().intValue());

        // 주문 저장
        orderService.saveOrder(order);

        // 임시 가정 응답
        // 주문 정보 외부 시스템 전송
        // externalOrderSender.send(order);
        ExternalSendInfo extInfo = new ExternalSendInfo(true, "주문 정보 전송 성공", "EXT-12345");

        // domain -> info
        return OrderInfo.from(order, extInfo);
    }

    @Transactional
    public OrderInfo placeOrderWithCard(OrderCommand command) {
        // 유저, 상품 조회
        User user = userService.getMyInfo(command.userId());
        List<Product> products = productService.getProductsByIdsWithLock(command.items()
                .stream().map(OrderItemCommand::productId).toList());
        List<OrderItem> items = OrderItemFactory.createFrom(command.items(), products);

        // 상품 재고 차감
        productService.checkAndDecreaseStock(items);

        // 쿠폰 조회 및 적용, 사용
        DiscountedOrderByCoupon discountedOrderByCoupon = couponService.useCoupon(command.userId(), command.couponId(), items);

        // 주문 생성
        Order order = orderService.createOrder(user, items, discountedOrderByCoupon);

        // 주문 저장
        orderService.saveOrder(order);

        // 결제
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CreatePaymentDto paymentDto = CreatePaymentDto.of(
                        user.getUserId(),
                        order.getId(),
                        CardType.valueOf(command.cardType()),
                        command.cardNo(),
                        order.getTotalPrice().longValue(),
                        CALLBACK_URL
                );

                // 결제 생성
                Payment payment = paymentService.createPayment(paymentDto);

                // 결제 요청
                paymentService.requestAndSavePayment(payment, CALLBACK_URL);
            }
        });

        // domain -> info
        return OrderInfo.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderInfo> getOrders(String userId) {
        // 유저 정보 조회
        User user = userService.getMyInfo(userId);

        // 주문 정보 목록 조회
        List<Order> orders = orderService.getOrders(user);

        // domain -> info
        return OrderInfo.from(orders);
    }

    public OrderInfo getOrderDetail(Long orderId, String userId) {
        // 유저 정보 조회
        User user = userService.getMyInfo(userId);

        // 주문 상세 정보 조회
        Order order = orderService.getOrderDetail(orderId, user);

        // domain -> info
        return OrderInfo.from(order);
    }

}
