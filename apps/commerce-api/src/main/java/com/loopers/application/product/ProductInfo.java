package com.loopers.application.product;

import com.loopers.domain.product.Product;


public record ProductInfo(
        Long id,
        String name,
        String description,
        String imageUrl,
        int price,
        Long likeCount,
        Long brandId,
        String brandName
) {

    public static ProductInfo from(Product product) {
        return new ProductInfo(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getLikeCount(),
                product.getBrand().getId(),
                product.getBrand().getName()
        );
    }

}
