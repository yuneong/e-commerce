package com.loopers.application.ranking;

import com.loopers.domain.product.Product;


public record RankingInfo(
        Long productId,
        String productName,
        String imageUrl,
        int price,
        Long likeCount,
        String brandName,
        Double score,
        int rank
) {

    public static RankingInfo from(Product product, Double score, int rank) {
        return new RankingInfo(
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                product.getPrice(),
                product.getLikeCount(),
                product.getBrand().getName(),
                score,
                rank
        );
    }

}
