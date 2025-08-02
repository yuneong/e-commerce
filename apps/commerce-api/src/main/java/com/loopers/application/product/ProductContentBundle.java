package com.loopers.application.product;

import com.loopers.domain.product.Product;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public record ProductContentBundle(
        List<ProductContent> contents
) {

    public static ProductContentBundle create(
            Page<Product> products,
            Map<Long, Long> likeCounts
    ) {
        List<ProductContent> productContents = products.getContent().stream()
                .map(product -> ProductContent.from(
                        product,
                        likeCounts.getOrDefault(product.getId(), 0L)
                ))
                .toList();

        return new ProductContentBundle(productContents);
    }

}
