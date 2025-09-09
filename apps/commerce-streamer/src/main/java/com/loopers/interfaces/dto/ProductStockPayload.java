package com.loopers.interfaces.dto;

public record ProductStockPayload(
        String eventId,
        Long productId,
        int stock,
        int price,
        String changedType
) {
}
