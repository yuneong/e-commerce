package com.loopers.domain.order;

import com.loopers.application.order.OrderItemCommand;
import com.loopers.application.order.OrderItemFactory;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PointRepository pointRepository;

    public Order createOrder(
            List<OrderItemCommand> itemCommands,
            User user,
            Point point,
            List<Product> products
    ) {
        // 주문 생성
        List<OrderItem> items = OrderItemFactory.createFrom(itemCommands, products);
        Order order = Order.place(user, items);

        // 상품 재고 차감 (검증 포함)
        items.forEach(item -> item.getProduct().decreaseStock(item.getQuantity()));

        // 포인트 차감 (검증 포함)
        Point usedPoint = point.use(order.calculateTotalPrice());
        // 주문 상태 변경
        order.updateOrderStatus(OrderStatus.PAID);

        // 주문 저장
        return save(order, usedPoint);
    }

    public Order save(Order order, Point point) {
        // 상품 저장
        List<Product> products = order.getOrderItems().stream()
                .map(OrderItem::getProduct)
                .toList();
        productRepository.saveAll(products);

        // 포인트 저장
        pointRepository.save(point);

        return orderRepository.save(order);
    }

    public List<Order> getOrders(User user) {
        return orderRepository.findByUser(user);
    }

    public Order getOrderDetail(Long orderId, User user) {
        return orderRepository.findByIdAndUser(orderId, user);
    }

}
