package com.loopers.application.order;


import java.util.List;

public record OrderCommand(
        String userId,
        List<OrderItemCommand> items,
        Long couponId,
        String cardType,
        String cardNo
) {

}
