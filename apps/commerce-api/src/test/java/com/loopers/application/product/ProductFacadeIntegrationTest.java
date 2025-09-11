package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.generator.RedisKeyGenerator;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductFacadeIntegrationTest {

    @Autowired private ProductFacade productFacade;
    @Autowired private ProductRepository productRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private RedisTemplate<String, String> redisTemplate;
    @Autowired private DatabaseCleanUp databaseCleanUp;
    @Autowired private RedisCleanUp redisCleanUp;

    private Product savedProduct;

    @BeforeEach
    void setup() {
        Brand brand = Brand.create("브랜드명", "브랜드설명", "http://brand-logo.jpg");
        Brand savedBrand = brandRepository.save(brand);

        savedProduct = productRepository.save(
                Product.create(savedBrand, "상품1", "상품설명1", "http://product1.jpg", 10000, 3)
        );
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    @Test
    @DisplayName("상품 존재 + 랭킹 존재 → rank 포함된 ProductInfo 반환")
    void getProductDetail_withRank() {
        // given
        String key = RedisKeyGenerator.todayKey("ranking:all:");
        redisTemplate.opsForZSet().add(key, savedProduct.getId().toString(), 50.0);

        // when
        ProductInfo info = productFacade.getProductDetail(savedProduct.getId(), "user-1");

        // then
        assertThat(info).isNotNull();
        assertThat(info.id()).isEqualTo(savedProduct.getId());
        assertThat(info.rank()).isNotNull();
    }

    @Test
    @DisplayName("상품 존재 + 랭킹 없음 → rank=null 반환")
    void getProductDetail_withoutRank() {
        // given
        String key = RedisKeyGenerator.todayKey("ranking:all:");
        redisTemplate.delete(key);

        // when
        ProductInfo info = productFacade.getProductDetail(savedProduct.getId(), "user-1");

        // then
        assertThat(info).isNotNull();
        assertThat(info.rank()).isNull();
    }
}
