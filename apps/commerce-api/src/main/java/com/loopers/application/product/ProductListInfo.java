package com.loopers.application.product;

import org.springframework.data.domain.Pageable;

import java.util.List;

public record ProductListInfo(
        List<ProductContent> contents,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static ProductListInfo from(
            ProductContentBundle bundle,
            Pageable pageable,
            long totalElements
    ) {
        return new ProductListInfo(
                bundle.contents(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                totalElements,
                (int) Math.ceil((double) totalElements / pageable.getPageSize())
        );
    }

}
