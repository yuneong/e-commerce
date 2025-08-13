package com.loopers.domain.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductCommand;
import com.loopers.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

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

    @Cacheable(value = "product", key = "'detail:' + #productId")
    public Product getProductDetailForCaching(Long productId) {
        // repository
        return productRepository.findWithBrandById(productId).orElseThrow(
                () -> new IllegalArgumentException("Product not found with id: " + productId)
        );
    }

    public Product getProductDetailForRedisTemplate(Long productId) throws JsonProcessingException {
        String key = "product:detail:" + productId;
        String json = "";

        // 캐시 조회
        json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            return objectMapper.readValue(json, Product.class); // 역직렬화
        }

        // DB 에서 조회
        Product productEntity = productRepository.findWithBrandById(productId).orElseThrow(
                () -> new IllegalArgumentException("Product not found with id: " + productId));

        // 캐시 저장
        json = objectMapper.writeValueAsString(productEntity); // 직렬화
        redisTemplate.opsForValue().set(key, json);

        return productEntity;
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

    public Long updateLikeCount(Long productId, String likeType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        switch (likeType.toLowerCase()) {
            case "like" -> product.increaseLikeCount();
            case "unlike" -> product.decreaseLikeCount();
            default -> throw new IllegalArgumentException("Invalid like type: " + likeType);
        }
        Product savedProduct = productRepository.save(product);

        return savedProduct.getLikeCount();
    }

    public Long getLikeCount(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        return product.getLikeCount();
    }

}
