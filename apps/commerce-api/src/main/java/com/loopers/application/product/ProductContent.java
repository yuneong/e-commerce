package com.loopers.application.product;

import com.loopers.domain.product.Product;

public record ProductContent(
        Long id,
        String name,
        String description,
        String imageUrl,
        int price,
        Long likeCount,
        Long brandId,
        String brandName
) {

    public static ProductContent from(Product product, Long likeCount) {
        return new ProductContent(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                likeCount,
                product.getBrand().getId(),
                product.getBrand().getName()
        );
    }
}
