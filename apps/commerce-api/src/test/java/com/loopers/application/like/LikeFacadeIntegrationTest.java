package com.loopers.application.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeStatus;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class LikeFacadeIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;

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
    private User testUser1;
    private User testUser3;

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
        testUser1 = userRepository.save(User.create("user1", Gender.F, "1999-08-21", "loopers@gmail.com"));
        User testUser2 = userRepository.save(User.create("user2", Gender.M, "1999-08-21", "loopers@gmail.com"));
        testUser3 = userRepository.save(User.create("user3", Gender.F, "1999-08-21", "loopers@gmail.com"));

        // product1,2 - 좋아요 등록 O
        likeRepository.save(Like.create(testUser1, testProductList.get(0)));
        testProductList.get(0).increaseLikeCount();
        likeRepository.save(Like.create(testUser1, testProductList.get(1)));
        testProductList.get(1).increaseLikeCount();
        likeRepository.save(Like.create(testUser2, testProductList.get(0)));
        testProductList.get(0).increaseLikeCount();
        likeRepository.save(Like.create(testUser2, testProductList.get(1)));
        testProductList.get(1).increaseLikeCount();

        testProductList = productRepository.saveAll(testProductList);

    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요 등록 시,")
    @Nested
    class LikeTest {

        @DisplayName("좋아요가 생성되고, 상품의 좋아요 토탈 수가 증가한다.")
        @Test
        void successLike_createsLikeAndIncreasesLikeCount() {
            // given
            User user = testUser3;
            Product product = testProductList.get(0);

            // when
            LikeInfo info = likeFacade.like(product.getId(), user.getUserId());

            // then
            Product updated = productRepository.findById(product.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(updated.getLikeCount()).isEqualTo(3L), // 2 + 1 (testUser1,2의 좋아요 포함)
                    () -> assertThat(info.likedYn()).isEqualTo(LikeStatus.Y)
            );
        }

        @DisplayName("이미 좋아요를 누른 상품에 대해 다시 좋아요를 누르면, 좋아요 수가 증가하지 않고, 기존 좋아요 정보가 반환된다.")
        @Test
        void success_duplicateLike_doesNotIncreaseCount() {
            // given
            User user = testUser1;
            Product product = testProductList.get(0);

            // when
            LikeInfo info = likeFacade.like(product.getId(), user.getUserId());

            // then
            Product updated = productRepository.findById(product.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(updated.getLikeCount()).isEqualTo(2L), // 기존 좋아요 수
                    () -> assertThat(info.likedYn()).isEqualTo(LikeStatus.Y)
            );
        }

    }


    @DisplayName("좋아요 취소 시,")
    @Nested
    class UnLikeTest {

        @DisplayName("좋아요가 취소되고, 상품의 좋아요 토탈 수가 감소한다.")
        @Test
        void successUnLike_removesLikeAndDecreasesLikeCount() {
            // given
            User user = testUser1;
            Product product = testProductList.get(0);

            // when
            LikeInfo info = likeFacade.unLike(product.getId(), user.getUserId());

            // then
            Product updated = productRepository.findById(product.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(updated.getLikeCount()).isEqualTo(1L), // 2 - 1 (testUser1의 좋아요 취소)
                    () -> assertThat(info.likedYn()).isEqualTo(LikeStatus.N)
            );
        }

        @DisplayName("이미 좋아요를 취소한 상품에 대해 다시 좋아요 취소를 시도하면, 좋아요 수가 감소하지 않고, 기존 좋아요 정보가 반환된다.")
        @Test
        void success_duplicateUnLike_doesNotDecreaseCount() {
            // given
            User user = testUser1;
            Product product = testProductList.get(0);
            // 좋아요 취소
            likeFacade.unLike(product.getId(), user.getUserId());

            // when
            LikeInfo info = likeFacade.unLike(product.getId(), user.getUserId());

            // then
            Product updated = productRepository.findById(product.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(updated.getLikeCount()).isEqualTo(1L),
                    () -> assertThat(info.likedYn()).isEqualTo(LikeStatus.N)
            );
        }

    }
}
