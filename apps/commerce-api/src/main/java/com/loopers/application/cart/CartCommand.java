package com.loopers.application.cart;


import java.util.List;

public record CartCommand(
        String userId,
        List<CartItemCommand> items
) {

}
