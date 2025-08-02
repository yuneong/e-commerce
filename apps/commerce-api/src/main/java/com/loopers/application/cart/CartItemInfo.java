package com.loopers.application.cart;


public record CartItemInfo(
        Long productId,
        String productName,
        String productImageUrl,
        int quantity,
        int price
) {

}
