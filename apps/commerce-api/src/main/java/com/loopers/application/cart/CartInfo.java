package com.loopers.application.cart;

import com.loopers.domain.cart.Cart;

import java.util.List;

public record CartInfo(
        Long cartId,
        String userId,
        int totalPrice,
        List<CartItemInfo> items
) {

    public static CartInfo from(Cart cart) {
        List<CartItemInfo> itemInfos = cart.getCartItems().stream()
                .map(item -> new CartItemInfo(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        item.getQuantity(),
                        item.getPrice()))
                .toList();

        return new CartInfo(
                cart.getId(),
                cart.getUser().getUserId(),
                cart.getTotalPrice(),
                itemInfos
        );
    }

}
