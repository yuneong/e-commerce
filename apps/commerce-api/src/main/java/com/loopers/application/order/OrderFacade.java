package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.DiscountedOrderByCoupon;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public OrderInfo placeOrder(OrderCommand command) {
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
        Order savedOrder = orderService.saveOrder(order);

        // domain -> info
        return OrderInfo.from(savedOrder);
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
