package com.loopers.interfaces.api.cart;

import com.loopers.application.cart.CartItemCommand;
import com.loopers.application.cart.CartItemInfo;

public class CartItemV1Dto {

    public record CartItemRequest(
            Long productId,
            int quantity,
            int price
    ) {
        public CartItemCommand toCommand() {
            return new CartItemCommand(
                    productId,
                    quantity,
                    price
            );
        }
    }

    public record CartItemResponse(
            Long productId,
            String productName,
            String productImageUrl,
            int quantity,
            int price
    ) {
        public static CartItemResponse from(CartItemInfo info) {
            return new CartItemResponse(
                    info.productId(),
                    info.productName(),
                    info.productImageUrl(),
                    info.quantity(),
                    info.price()
            );
        }
    }

}
