package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderItemCommand;
import com.loopers.application.order.OrderItemInfo;

public class OrderItemV1Dto {

    public record OrderItemRequest(
            Long productId,
            int quantity,
            int price
    ) {
        public OrderItemCommand toCommand() {
            return new OrderItemCommand(
                    productId,
                    quantity,
                    price
            );
        }
    }

    public record OrderItemResponse(
            Long productId,
            String productName,
            int quantity,
            int price
    ) {
        public static OrderItemResponse from(OrderItemInfo info) {
            return new OrderItemResponse(
                    info.productId(),
                    info.productName(),
                    info.quantity(),
                    info.price()
            );
        }
    }

}
