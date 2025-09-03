package com.loopers.infrastructure.kafka.dto;

public record LikeChangedDto (
        Long productId,
        String likeType
) {

    public static LikeChangedDto of(
            Long productId,
            String likeType
    ) {
        return new LikeChangedDto(
                productId,
                likeType
        );
    }

}
