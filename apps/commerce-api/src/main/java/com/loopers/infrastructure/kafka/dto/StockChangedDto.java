package com.loopers.infrastructure.kafka.dto;

public record StockChangedDto(
        Long productId,
        int stock,
        String changedType
) {

    public static StockChangedDto of(
            Long productId,
            int stock,
            String changedType
    ) {
        return new StockChangedDto(
                productId,
                stock,
                changedType
        );
    }

}
