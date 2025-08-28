package com.loopers.domain.order.event;


public record OrderPlacedEvent(
        Long orderId,
        int totalPrice
) {

    public static OrderPlacedEvent of (
            Long orderId,
            int totalPrice
    ) {
        return new OrderPlacedEvent(orderId, totalPrice);
    }

}
