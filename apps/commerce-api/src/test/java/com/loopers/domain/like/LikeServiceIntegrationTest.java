package com.loopers.domain.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class LikeServiceIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private List<Product> testProductList;

    @BeforeEach
    void setUp() {
        // 상품 생성
        Brand testBrand = brandRepository.save(Brand.create(
                "브랜드명",
                "브랜드설명",
                "https://example.com/brand-image.jpg"
        ));

        List<Product> products = IntStream.rangeClosed(1, 4)
                .mapToObj(i -> Product.create(
                        testBrand,
                        "상품" + i,
                        "상품설명",
                        "https://example.com/product-image.jpg",
                        i * 1000,
                        10
                ))
                .toList();

        testProductList = productRepository.saveAll(products);

        // 유저 생성
        User user1 = userRepository.save(User.create("user1", Gender.F, "1999-08-21", "loopers@gmail.com"));
        User user2 = userRepository.save(User.create("user2", Gender.M, "1999-08-21", "loopers@gmail.com"));
        User user3 = userRepository.save(User.create("user3", Gender.F, "1999-08-21", "loopers@gmail.com"));

        // product1,2는 좋아요 등록 O, product3,4는 좋아요 등록 X
        likeRepository.save(Like.create(user1, testProductList.get(0)));
        likeRepository.save(Like.create(user1, testProductList.get(1)));
        likeRepository.save(Like.create(user2, testProductList.get(0)));
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품별 좋아요 수 조회 시,")
    @Nested
    class getLikeCounts {

        @DisplayName("상품 ID 목록이 비어있다면 빈 맵을 반환한다.")
        @Test
        void returnsEmptyMap_whenProductIdsIsEmpty() {
            // given
            List<Long> productIds = List.of();

            // when
            Map<Long, Long> result = likeService.getLikeCounts(productIds);

            // then
            assertThat(result).isEmpty();
        }

        @DisplayName("해당 상품에 좋아요가 없다면 0을 반환한다.")
        @Test
        void returnsZero_whenProductHasNoLikes() {
            // given
            List<Long> productIds = List.of(3L, 4L);

            // when
            Map<Long, Long> result = likeService.getLikeCounts(productIds);

            // then
            assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(3L)).isEqualTo(0L),
                () -> assertThat(result.get(4L)).isEqualTo(0L)
            );
        }

        @DisplayName("해당 상품에 좋아요가 있다면 좋아요 수를 반환한다.")
        @Test
        void returnLikeCount_whenLikesExist() {
            // given
            List<Long> productIds = testProductList.stream().map(Product::getId).toList();

            // when
            Map<Long, Long> result = likeService.getLikeCounts(productIds);

            // then
            assertAll(
                () -> assertThat(result).hasSize(4),
                () -> assertThat(result.get(1L)).isGreaterThan(0L),
                () -> assertThat(result.get(2L)).isGreaterThan(0L),
                () -> assertThat(result.get(3L)).isEqualTo(0L),
                () -> assertThat(result.get(4L)).isEqualTo(0L)
            );
        }
    }


    @DisplayName("단일 상품 좋아요 수 조회 시,")
    @Nested
    class getLikeCount {

        @DisplayName("해당 상품이 존재하면 좋아요 수를 반환한다.")
        @Test
        void returnLikeCount_whenProductExist() {
            // given
            Long productId = testProductList.get(0).getId();

            // when
            Long likeCount = likeService.getLikeCount(productId);

            // then
            assertThat(likeCount).isGreaterThan(0L);
            assertThat(likeCount).isEqualTo(2L);
        }

        @DisplayName("해당 상품이 존재하지 않으면 0을 반환한다.")
        @Test
        void returnZero_whenProductIsNotExist() {
            // given
            Long nonExistingProductId = 999L; // 존재하지 않는 상품 ID

            // when
            Long likeCount = likeService.getLikeCount(nonExistingProductId);

            // then
            assertThat(likeCount).isEqualTo(0L);
        }
    }


    @DisplayName("좋아요 등록 시,")
    @Nested
    @Transactional
    class like {

        @DisplayName("좋아요가 없는 상품이라면 새로 등록한다.")
        @Test
        void createNewLike_whenNotLikedBefore() {
            // given
            Product product = testProductList.get(2); // 좋아요가 등록되지 않은 상품
            Optional<User> user = userRepository.findByUserId("user1");

            // when
            LikeChange savedLike = likeService.like(product, user.get());

            // then
            assertAll(
                    () -> assertThat(savedLike).isNotNull(),
                    () -> assertThat(savedLike.like().getProduct().getId()).isEqualTo(product.getId()),
                    () -> assertThat(savedLike.like().getUser().getUserId()).isEqualTo(user.get().getUserId()),
                    () -> assertThat(savedLike.like().getLikedYn()).isEqualTo(LikeStatus.Y),
                    () -> assertThat(savedLike.changed()).isTrue()
            );
        }

        @DisplayName("이미 좋아요한 상품이면 상태를 Y로 유지한다.")
        @Test
        void maintainsLike_whenAlreadyLiked() {
            // given
            Product product = testProductList.get(0); // 이미 좋아요가 등록된 상품
            Optional<User> user = userRepository.findByUserId("user1");

            // when
            LikeChange savedLike = likeService.like(product, user.get());

            // then
            assertAll(
                    () -> assertThat(savedLike).isNotNull(),
                    () -> assertThat(savedLike.like().getProduct().getId()).isEqualTo(product.getId()),
                    () -> assertThat(savedLike.like().getUser().getUserId()).isEqualTo(user.get().getUserId()),
                    () -> assertThat(savedLike.like().getLikedYn()).isEqualTo(LikeStatus.Y),
                    () -> assertThat(savedLike.changed()).isFalse()
            );
        }
    }


    @DisplayName("좋아요 취소 시,")
    @Nested
    @Transactional
    class unLike {

        @DisplayName("좋아요가 등록된 상품이면 좋아요를 취소한다.")
        @Test
        void setLikeStatusToN_whenAlreadyLikedProduct() {
            // given
            Product product = testProductList.get(0); // 좋아요가 등록된 상품
            Optional<User> user = userRepository.findByUserId("user1");

            // when
            LikeChange savedLike = likeService.unLike(product, user.get());

            // then
            assertAll(
                    () -> assertThat(savedLike).isNotNull(),
                    () -> assertThat(savedLike.like().getProduct().getId()).isEqualTo(product.getId()),
                    () -> assertThat(savedLike.like().getUser().getUserId()).isEqualTo(user.get().getUserId()),
                    () -> assertThat(savedLike.like().getLikedYn()).isEqualTo(LikeStatus.N),
                    () -> assertThat(savedLike.changed()).isTrue()
            );
        }

        @DisplayName("좋아요가 없는 상품을 취소하면 예외가 발생한다.")
        @Test
        void throwsIllegalArgumentException_whenUnLikeNonLikedProduct() {
            // given
            Product product = testProductList.get(2); // 좋아요가 등록되지 않은 상품
            Optional<User> user = userRepository.findByUserId("user1");

            // when & then
            assertThrows(IllegalArgumentException.class, () -> {
                likeService.unLike(product, user.get());
            });
        }
    }


    @DisplayName("좋아요 한 상품 목록 조회 시,")
    @Nested
    class getLikeProducts {

        @DisplayName("해당 유저가 좋아요한 상품 목록을 반환한다.")
        @Test
        @Transactional
        void returnLikeProductList() {
            // given
            Optional<User> user = userRepository.findByUserId("user1");

            // when
            List<Like> likeProducts = likeService.getLikeProducts(user.get());

            // then
            assertAll(
                    () -> assertThat(likeProducts).isNotEmpty(),
                    () -> assertThat(likeProducts.size()).isEqualTo(2),
                    () -> assertThat(likeProducts.get(0).getUser().getUserId()).isEqualTo(user.get().getUserId()),
                    () -> assertThat(likeProducts.get(0).getLikedYn()).isEqualTo(LikeStatus.Y),
                    () -> assertThat(likeProducts.get(1).getUser().getUserId()).isEqualTo(user.get().getUserId()),
                    () -> assertThat(likeProducts.get(1).getLikedYn()).isEqualTo(LikeStatus.Y)
            );
        }

        @DisplayName("해당 유저가 좋아요한 상품이 없는 경우, 빈 리스트를 반환한다.")
        @Test
        void returnEmptyList_whenUserHasNoLikes() {
            // given
            Optional<User> user = userRepository.findByUserId("user3");

            // when
            List<Like> likeProducts = likeService.getLikeProducts(user.get());

            // then
            assertAll(
                    () -> assertThat(likeProducts).isNotNull(),
                    () -> assertThat(likeProducts).isEmpty()
            );
        }
    }

}
