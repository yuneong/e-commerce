package com.loopers.application.cart;


public record CartItemCommand(
        Long productId,
        int quantity,
        int price
) {

}
