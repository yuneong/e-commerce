package com.loopers.application.product;

import com.loopers.domain.product.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public record ProductListInfo(
        List<Product> contents,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static ProductListInfo from(
            Page<Product> products
    ) {
        return new ProductListInfo(
                products.getContent(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages()
        );
    }

}
