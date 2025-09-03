package com.loopers.interfaces.dto;

public record ProductStockPayload(
        Long productId,
        int stock,
        String changedType
) {
}
