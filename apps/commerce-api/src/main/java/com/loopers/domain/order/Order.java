package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    private BigDecimal totalPrice;

    private OrderStatus status;

    private ZonedDateTime paidAt;

    private Long couponId;

    public static Order place(User user, List<OrderItem> items, DiscountedOrderByCoupon discountedOrderByCoupon) {
        Order order = new Order();

        order.validate(user, items);

        order.user = user;
        for (OrderItem item : items) {
            order.addItem(item);
        }
        order.totalPrice = discountedOrderByCoupon.discountedTotalPrice();
        order.status = OrderStatus.CREATED;
        order.paidAt = ZonedDateTime.now();
        order.couponId = discountedOrderByCoupon.couponId();

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

    public void updateOrderStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("주문 상태는 null일 수 없습니다.");
        }

        this.status = status;
    }

}
