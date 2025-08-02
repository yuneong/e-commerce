package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductContent;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductListInfo;

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
        public static ProductContentResponse from(ProductContent content) {
            return new ProductContentResponse(
                    content.id(),
                    content.name(),
                    content.description(),
                    content.imageUrl(),
                    content.price(),
                    content.likeCount(),
                    content.brandId(),
                    content.brandName()
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
