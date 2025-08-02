package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    int totalPrice;

    OrderStatus status;

    ZonedDateTime paidAt;

    public static Order place(User user, List<OrderItem> items) {
        Order order = new Order();

        order.validate(user, items);

        order.user = user;
        for (OrderItem item : items) {
            order.addItem(item);
        }
        order.totalPrice = order.calculateTotalPrice();
        order.status = OrderStatus.PLACED;
        order.paidAt = ZonedDateTime.now();

        return order;
    }

    public void validate(User user, List<OrderItem> items) {
        if (user == null) {
            throw new NullPointerException("주문시 사용자 정보는 필수입니다.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문 아이템은 필수입니다.");
        }
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("주문 아이템은 null일 수 없습니다.");
        }

        orderItems.add(item);
    }

    public int calculateTotalPrice() {
        return orderItems.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public void updateOrderStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("주문 상태는 null일 수 없습니다.");
        }

        this.status = status;
    }

}
