package com.loopers.interfaces.api.cart;


import com.loopers.application.cart.CartCommand;
import com.loopers.application.cart.CartInfo;
import com.loopers.application.cart.CartItemCommand;

import java.util.List;

public class CartV1Dto {

    public record CartRequest(
            List<CartItemV1Dto.CartItemRequest> items
    ) {
        public CartCommand toCommand(String userId) {
            List<CartItemCommand> itemCommands = this.items.stream()
                    .map(CartItemV1Dto.CartItemRequest::toCommand)
                    .toList();

            return new CartCommand(userId, itemCommands);
        }
    }

    public record CartResponse(
            String status,
            String message
    ) {

        public static CartResponse fail() {
            return new CartResponse("fail", "장바구니에 상품을 추가할 수 없습니다.");
        }

        public static CartResponse success() {
            return new CartResponse("success", "장바구니에 상품을 추가했습니다.");
        }

    }

    public record CartItemListResponse(
            String userId,
            int totalPrice,
            List<CartItemV1Dto.CartItemResponse> items
    ) {
        public static CartItemListResponse from(CartInfo info) {
            List<CartItemV1Dto.CartItemResponse> itemResponses = info.items().stream()
                    .map(CartItemV1Dto.CartItemResponse::from)
                    .toList();

            return new CartItemListResponse(
                    info.userId(),
                    info.totalPrice(),
                    itemResponses
            );
        }

    }

}
