package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
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
    private final PointService pointService;

    @Transactional
    public OrderInfo placeOrder(OrderCommand command) {
        // 유저, 포인트 조회
        User user = userService.getMyInfo(command.userId());
        Point point = pointService.getPoint(user);

        // 상품 조회
        List<Long> productIds = command.items().stream()
                .map(OrderItemCommand::productId)
                .toList();
        List<Product> products = productService.getProductsByIds(productIds);

        Order order = orderService.createOrder(command.items(), user, point, products);

        // 임시 가정 응답
        // 주문 정보 외부 시스템 전송
        // externalOrderSender.send(order);
        ExternalSendInfo extInfo = new ExternalSendInfo(true, "주문 정보 전송 성공", "EXT-12345");

        // domain -> info
        return OrderInfo.from(order, extInfo);
    }

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
