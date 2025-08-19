package com.loopers.interfaces.api.order;


import com.loopers.application.order.ExternalSendInfo;
import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderItemCommand;
import com.loopers.domain.order.OrderStatus;

import java.util.List;

public class OrderV1Dto {

    public record OrderRequest(
            List<OrderItemV1Dto.OrderItemRequest> items,
            Long couponId,
            String cardType,
            String cardNo
    ) {
        public OrderCommand toCommand(String userId) {
            List<OrderItemCommand> itemCommands = this.items.stream()
                    .map(OrderItemV1Dto.OrderItemRequest::toCommand)
                    .toList();

            return new OrderCommand(userId, itemCommands, couponId, cardType, cardNo);
        }

    }

    public record OrderResponse(
            Long orderId,
            String userId,
            int totalPrice,
            OrderStatus orderStatus,
            List<OrderItemV1Dto.OrderItemResponse> items,
            ExternalSendInfo externalSendInfo
    ) {
        public static OrderResponse from(OrderInfo info) {
            List<OrderItemV1Dto.OrderItemResponse> itemResponses = info.items().stream()
                    .map(OrderItemV1Dto.OrderItemResponse::from)
                    .toList();

            return new OrderResponse(
                    info.orderId(),
                    info.userId(),
                    info.totalPrice(),
                    info.orderStatus(),
                    itemResponses,
                    info.externalSendInfo()
            );
        }

    }

    public record OrderListResponse(
            List<OrderResponse> orders
    ) {

        public static OrderListResponse from(List<OrderInfo> infos) {
            List<OrderResponse> responses = infos.stream()
                    .map(OrderResponse::from)
                    .toList();

            return new OrderListResponse(responses);
        }
    }

}
