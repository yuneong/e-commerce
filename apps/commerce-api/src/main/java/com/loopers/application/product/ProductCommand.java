package com.loopers.application.product;


import com.loopers.domain.product.ProductSearchCondition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ProductCommand(
        Long brandId,
        int page,
        int size,
        Sort sort
) {

    public static ProductCommand of(Long brandId, Pageable pageable) {
        return new ProductCommand(
                brandId,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
    }

    public Pageable toPageable() {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : size;
        Sort safeSort = (sort == null || sort().isEmpty())
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : sort;

        return PageRequest.of(safePage, safeSize, safeSort);
    }

    public ProductSearchCondition toCondition() {
        return new ProductSearchCondition(
                brandId,
                toPageable()
        );
    }

}
