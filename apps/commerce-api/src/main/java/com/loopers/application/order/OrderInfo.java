package com.loopers.application.order;


import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;

import java.util.List;

public record OrderInfo(
        Long orderId,
        String userId,
        int totalPrice,
        OrderStatus orderStatus,
        List<OrderItemInfo> items,
        ExternalSendInfo externalSendInfo,
        Long couponId
) {

    public static OrderInfo from(
            Order order,
            ExternalSendInfo externalSendInfo
    ) {

        List<OrderItemInfo> itemInfos = order.getOrderItems().stream()
                .map(item -> new OrderItemInfo(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice()
                )).toList();

        return new OrderInfo(
                order.getId(),
                order.getUser().getUserId(),
                order.getTotalPrice().intValue(),
                order.getStatus(),
                itemInfos,
                externalSendInfo,
                order.getCouponId()
        );
    }

    public static List<OrderInfo> from(List<Order> orders) {
        return orders.stream()
                .map(order -> from(order, null))
                .toList();
    }

    public static OrderInfo from(Order order) {
        return from(order, null);
    }

}
