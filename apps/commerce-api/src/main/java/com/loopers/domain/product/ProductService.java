package com.loopers.domain.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductCommand;
import com.loopers.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    @Value("${cache.version.product}") public static String CACHE_VERSION;
    private final CacheManager cacheManager;

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

    @Cacheable(value = "product", key = "@productService.CACHE_VERSION + ':detail:' + #productId")
    public Product getProductDetailForCaching(Long productId) {
        // repository
        return productRepository.findWithBrandById(productId).orElseThrow(
                () -> new IllegalArgumentException("Product not found with id: " + productId)
        );
    }

    public Product getProductDetailForRedisTemplate(Long productId) throws JsonProcessingException {
        String key = "product:" + CACHE_VERSION + ":detail:" + productId;
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
    public List<Product> getProductsByIdsWithLock(List<Long> productIds) {
        // repository
        return productRepository.findAllWithLock(productIds);
    }

    @Transactional
    public void checkAndDecreaseStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            int stock = product.decreaseStock(orderItem.getQuantity());

            productRepository.save(product); // 재고 차감 후 저장

            if (stock == 0) { // 재고 0일때 캐시 삭제
                Cache cache = cacheManager.getCache("product");
                if (cache != null) {
                    cache.evict(CACHE_VERSION + ":detail:" + product.getId());
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(
            retryFor = {
                    org.springframework.dao.OptimisticLockingFailureException.class,
                    jakarta.persistence.OptimisticLockException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 10, multiplier = 2.0, maxDelay = 200)
    )
    @CacheEvict(value = "product", key = "@productService.CACHE_VERSION + ':detail:' + #productId")
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

    @Transactional
    @Retryable(
            retryFor = {
                    org.springframework.dao.OptimisticLockingFailureException.class,
                    jakarta.persistence.OptimisticLockException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 10, multiplier = 2.0, maxDelay = 200)
    )
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        product.restoreStock(quantity);

        productRepository.save(product);
    }

}
