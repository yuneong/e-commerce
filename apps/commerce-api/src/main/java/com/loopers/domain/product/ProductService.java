package com.loopers.domain.product;

import com.loopers.application.product.ProductCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> getProducts(ProductCommand command) {
        // command -> domain
        ProductSearchCondition condition = command.toCondition();
        // repository
        return productRepository.findByCondition(condition);
    }

    public Product getProductDetail(Long productId) {
        // repository
        return productRepository.findById(productId).orElseThrow(
                () -> new IllegalArgumentException("Product not found with id: " + productId)
        );
    }

    public List<Product> getProductsByIds(List<Long> productIds) {
        // repository
        return productRepository.findAllById(productIds);
    }

}
