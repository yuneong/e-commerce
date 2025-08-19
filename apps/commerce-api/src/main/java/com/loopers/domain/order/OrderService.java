package com.loopers.domain.order;

import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(
            User user,
            List<OrderItem> items,
            DiscountedOrderByCoupon discountedOrderByCoupon
    ) {
        Order order = Order.place(user, items, discountedOrderByCoupon);

        return orderRepository.save(order);
    }

    @Transactional
    public Order saveOrder(Order order) {
        // 주문 상태 변경
        order.updateOrderStatus(OrderStatus.PLACED);

        return orderRepository.save(order);
    }

    public List<Order> getOrders(User user) {
        return orderRepository.findByUser(user);
    }

    public Order getOrderDetail(Long orderId, User user) {
        return orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId: " + orderId));
    }

}
