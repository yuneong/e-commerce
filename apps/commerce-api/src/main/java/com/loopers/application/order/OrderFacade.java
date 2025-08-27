package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderSucceededEvent;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderInfo placeOrder(OrderCommand command) {
        // 유저 조회
        User user = userService.getMyInfo(command.userId());

        // 상품 조회 + 재고 체크 및 차감
        List<Product> products = productService.getProductsByIdsWithLock(command.items()
                .stream().map(OrderItemCommand::productId).toList());
        List<OrderItem> items = OrderItemFactory.createFrom(command.items(), products);
        productService.checkAndDecreaseStock(items);

        // 할인 및 결제 금액 계산
        int itemsPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();
        int discountAmount = couponService.calculateDiscountAmount(command.userId(), command.couponId(), itemsPrice);
        int totalPrice = Math.max(0, itemsPrice - discountAmount);

        // 주문 생성
        Order order = orderService.createOrder(user, items, totalPrice, command.couponId());

        // 이벤트 발행
        eventPublisher.publishEvent(new OrderSucceededEvent(
                user.getUserId(),
                order.getId()
        ));

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
