package com.loopers.domain.product;


import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Page<Product> findByCondition(ProductSearchCondition condition);

    Optional<Product> findById(Long productId);

    List<Product> saveAll(List<Product> products);

    List<Product> findAllById(List<Long> productIds);

    List<Product> findAllWithLock(List<Long> productIds);

    Product save(Product product);

    List<Product> findByAll();

}
