package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Page<Product> findByCondition(ProductSearchCondition condition) {
        if (condition.brandId() != null) {
            return productJpaRepository.findByBrandId(
                    condition.brandId(),
                    condition.toPageRequest()
            );
        } else {
            return productJpaRepository.findAll(condition.toPageRequest());
        }
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public Optional<Product> findWithBrandById(Long productId) {
        return productJpaRepository.findWithBrandById(productId);
    }

    @Override
    public List<Product> saveAll(List<Product> products) {
        return productJpaRepository.saveAll(products);
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return productJpaRepository.findAllById(productIds);
    }

    @Override
    public List<Product> findAllWithLock(List<Long> productIds) {
        return productJpaRepository.findAllWithLock(productIds);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<Product> findByAll() {
        return productJpaRepository.findAll();
    }

}
