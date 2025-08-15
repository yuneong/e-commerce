package com.loopers.domain.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ProductRedisTest {

    @MockitoSpyBean
    private ProductService productService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private RedisCleanUp redisCleanUp;

    private Brand testBrand;
    private Product testProduct;

    @BeforeEach
    void setupTestData() {
        testBrand = brandRepository.save(Brand.create(
                "브랜드명",
                "브랜드설명",
                "https://example.com/brand-image.jpg"
        ));

        testProduct = productRepository.save(Product.create(
                testBrand,
                "상품명",
                "상품설명",
                "https://example.com/product-image.jpg",
                1000,
                10
        ));
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    @DisplayName("@Cacheable - 상품 상세 조회 시,")
    @Nested
    class getProductDetailForCaching {

        @DisplayName("캐시 없으면 DB 조회 + 캐시 저장, 캐시 있으면 캐시 조회만 한다.")
        @Test
        void cacheHitAndMiss() {
            // given
            Product product = testProduct;

            // when
            // 첫 호출 -> 캐시 미스 -> DB 조회 -> 캐시 저장
            Product firstCall = productService.getProductDetailForCaching(product.getId());

            // 두 번째 호출 -> 캐시 히트
            Product secondCall = productService.getProductDetailForCaching(product.getId());

            // then
            assertThat(firstCall.getName()).isEqualTo(secondCall.getName());
            verify(productService, times(1)).getProductDetailForCaching(product.getId());
        }

        @DisplayName("서로 다른 상품 ID는 각각 별도의 캐시로 저장된다.")
        @Test
        void shouldCacheSeparatelyForDifferentProductIds() {
            // given
            Product product1 = testProduct;
            Product product2 = productRepository.save(Product.create(
                    testBrand, "다른상품", "설명", "https://example.com/product-image.jpg", 2000, 10));

            // when
            Product result1 = productService.getProductDetailForCaching(product1.getId());
            Product result2 = productService.getProductDetailForCaching(product2.getId());

            // then
            assertThat(result1.getId()).isEqualTo(product1.getId());
            assertThat(result2.getId()).isEqualTo(product2.getId());
            verify(productService, times(1)).getProductDetailForCaching(product1.getId());
            verify(productService, times(1)).getProductDetailForCaching(product2.getId());
        }

        @DisplayName("존재하지 않는 ID로 조회하면 예외 발생하고 캐시는 저장되지 않는다.")
        @Test
        void shouldThrowExceptionAndNotCache_whenIdNotFound() {
            // given
            Long invalidId = -999L;
            String key = "detail:" + invalidId;

            // when & then
            assertThatThrownBy(() -> productService.getProductDetailForCaching(invalidId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product not found with id");

            // 캐시 저장 안된거 확인
            // @Cacheable은 캐시 객체 를 애플리케이션 시작 시점에 등록 -> 데이터가 존재하지 않아도 캐시 객체는 존재함
            Cache cache = cacheManager.getCache("product");
            assertThat(cache).isNotNull(); // 캐시 객체는 존재하므로 notNull
            assertThat(cache.get(key)).isNull(); // 캐시에 해당 key는 미존재
        }

        @DisplayName("캐시가 존재하다가 evict()로 삭제되면, 다음 조회 시 DB 재조회 후 캐시가 다시 저장된다.")
        @Test
        void shouldReloadFromDbAfterEvictingCache() {
            // given
            Product product = testProduct;
            Long productId = product.getId();
            String key = "detail:" + productId;

            // when
            // 최초 조회 → 캐시 저장
            Product firstCall = productService.getProductDetailForCaching(productId);

            // 캐시에서 제거 (@CacheEvict 기능 구현)
            Objects.requireNonNull(cacheManager.getCache("product")).evict(key);

            // 2번째 조회 → 캐시 미스 → DB 조회 → 캐시 저장
            Product secondCall = productService.getProductDetailForCaching(productId);

            // then
            assertThat(secondCall.getName()).isEqualTo(firstCall.getName());
            assertThat(secondCall.getId()).isEqualTo(productId);
            verify(productService, times(2)).getProductDetailForCaching(productId);
        }

    }

    @DisplayName("redisTemplate - 상품 상세 조회 시,")
    @Nested
    class getProductDetailForRedisTemplate {

        @DisplayName("캐시 없으면 DB 조회 + 캐시 저장, 캐시 있으면 캐시 조회만 한다.")
        @Test
        void cacheHitAndMiss() throws JsonProcessingException {
            // given
            Product product = testProduct;
            String key = "product:detail:" + product.getId();
            Product firstCall = productService.getProductDetailForRedisTemplate(product.getId());

            // when
            String json = redisTemplate.opsForValue().get(key);
            Product productForRedis = objectMapper.readValue(json, Product.class);

            // then
            assertAll(
                    () -> assertThat(productForRedis).isNotNull(),
                    () -> assertThat(productForRedis.getName()).isEqualTo(firstCall.getName()),
                    () -> assertThat(productForRedis.getId()).isEqualTo(firstCall.getId())
            );
        }

        @DisplayName("TTL 설정된 캐시가 만료되면 다시 DB 조회 후 캐시를 갱신한다.")
        @Test
        void shouldReloadFromDbAndRecache_whenTtlExpires() throws Exception {
            // given
            Product product = testProduct;
            String key = "product:detail:" + product.getId();

            // 최초 조회 -> DB 조회 -> 캐시 저장
            Product firstCall = productService.getProductDetailForRedisTemplate(product.getId());

            // TTL 1초 설정 -> 캐시 저장
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(firstCall), 1, TimeUnit.SECONDS);

            // 1.5초 대기 -> 캐시 만료
            Thread.sleep(1500);

            // 2번째 조회 -> 캐시 미스 → DB 조회 -> 캐시 저장
            Product secondCall = productService.getProductDetailForRedisTemplate(product.getId());

            // when
            String json = redisTemplate.opsForValue().get(key);
            Product productForRedis = objectMapper.readValue(json, Product.class);

            // then
            assertAll(
                    () -> assertThat(productForRedis).isNotNull(),
                    () -> assertThat(productForRedis.getName()).isEqualTo(secondCall.getName()),
                    () -> assertThat(productForRedis.getId()).isEqualTo(secondCall.getId())
            );
            verify(productService, times(2)).getProductDetailForRedisTemplate(product.getId());
        }

        @DisplayName("존재하지 않는 ID 조회 시 예외 발생, 캐시는 저장되지 않는다.")
        @Test
        void shouldThrowExceptionAndNotCache_whenProductNotFound() {
            // given
            Long invalidId = -999L;
            String key = "product:detail:" + invalidId;

            // when & then
            assertThatThrownBy(() -> productService.getProductDetailForRedisTemplate(invalidId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product not found with id");

            // 캐시 저장 안된거 확인
            // redisTemplate.opsForValue()는 캐시 객체 생성 개념 아님
            String cache = redisTemplate.opsForValue().get(key);
            assertThat(cache).isNull(); // 해당 key로 저장된 캐시가 없음
        }

        @DisplayName("DB 조회 후 직렬화된 JSON이 캐시에 올바르게 저장된다.")
        @Test
        void shouldSerializeAndSaveJsonToRedis() throws Exception {
            // given
            Product product = testProduct;
            String key = "product:detail:" + product.getId();

            // when
            productService.getProductDetailForRedisTemplate(product.getId());

            // then
            String json = redisTemplate.opsForValue().get(key);
            assertThat(json).isNotEmpty();
            Product cache = objectMapper.readValue(json, Product.class);
            assertThat(cache.getId()).isEqualTo(product.getId());
            assertThat(cache.getName()).isEqualTo(product.getName());
        }

    }

}
