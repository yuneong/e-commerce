package com.loopers.interfaces.dto;

public record ProductLikePayload (
        Long productId,
        String likeType
) {
}
