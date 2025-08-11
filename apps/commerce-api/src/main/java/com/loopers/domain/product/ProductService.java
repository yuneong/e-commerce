package com.loopers.domain.product;

import com.loopers.application.product.ProductCommand;
import com.loopers.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public List<Product> getProductsByIds(List<Long> productIds) {
        // repository
        return productRepository.findAllWithLock(productIds);
    }

    @Transactional
    public void checkAndDecreaseStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Long productId = orderItem.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId)); // 영속성 보장을 위함

            product.decreaseStock(orderItem.getQuantity());
        }
    }

}
