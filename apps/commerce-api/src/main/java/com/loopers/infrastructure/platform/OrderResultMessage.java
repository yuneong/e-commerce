package com.loopers.infrastructure.platform;


public record OrderResultMessage(
        String userId,
        Long orderId,
        String status
) {
}
