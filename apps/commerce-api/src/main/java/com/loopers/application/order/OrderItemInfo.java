package com.loopers.application.order;


public record OrderItemInfo(
        Long productId,
        String productName,
        int quantity,
        int price
) {

}
