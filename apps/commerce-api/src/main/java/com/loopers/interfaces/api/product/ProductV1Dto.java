package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductListInfo;
import com.loopers.domain.product.Product;

import java.util.List;

public class ProductV1Dto {

    public record ProductListResponse(
            List<ProductContentResponse> contents,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static ProductListResponse from(ProductListInfo info) {
            List<ProductContentResponse> contentResponses = info.contents().stream()
                    .map(ProductContentResponse::from)
                    .toList();

            return new ProductListResponse(
                    contentResponses,
                    info.page(),
                    info.size(),
                    info.totalElements(),
                    info.totalPages()
            );
        }
    }

    public record ProductContentResponse(
            Long id,
            String name,
            String description,
            String imageUrl,
            int price,
            Long likeCount,
            Long brandId,
            String brandName
    ) {
        public static ProductContentResponse from(Product product) {
            return new ProductContentResponse(
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

    public record ProductDetailResponse(
            Long id,
            String name,
            String description,
            String imageUrl,
            int price,
            Long likeCount,
            Long brandId,
            String brandName
    ) {
        public static ProductDetailResponse from(ProductInfo info) {
            return new ProductDetailResponse(
                    info.id(),
                    info.name(),
                    info.description(),
                    info.imageUrl(),
                    info.price(),
                    info.likeCount(),
                    info.brandId(),
                    info.brandName()
            );
        }
    }

}
