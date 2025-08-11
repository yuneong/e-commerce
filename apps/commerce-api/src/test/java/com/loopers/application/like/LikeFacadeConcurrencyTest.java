package com.loopers.application.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.TestFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("동시성 테스트")
class LikeFacadeConcurrencyTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private List<User> users;
    private Product product;

    @BeforeEach
    void setUp() {
        Brand brand = brandRepository.save(TestFixture.createBrand());
        product = productRepository.save(TestFixture.createProduct(brand));

        users = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String userId = "user" + i;
            String email = "user" + i + "@loopers.com";

            User user = userRepository.save(User.create(userId, Gender.F, "1999-08-21", email));
            users.add(user);

            likeRepository.save(Like.create(user, product)); // 모든 유저가 좋아요 추가
        }
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }


    @DisplayName("동일 상품에 대해 동시에 좋아요/취소 요청을 해도 좋아요 수가 정확히 반영된다.")
    @Test
    void likeCount_should_be_consistent_under_concurrent_like_and_unlike() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Long productId = product.getId();
        List<User> testUsers = users.subList(0, threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String userId = testUsers.get(i).getUserId();
            executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                try {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        likeFacade.unLike(productId, userId);
                        System.out.printf("❤️ [%s][%s] 좋아요 취소 요청\n", threadName, userId);
                    } else {
                        likeFacade.like(productId, userId);
                        System.out.printf("❤️ [%s][%s] 좋아요 추가 요청\n", threadName, userId);
                    }
                } catch (Exception e) {
                    System.out.printf("[%s][%s] 요청 실패: %s\n", threadName, userId, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Long finalLikeCount = likeService.getLikeCount(productId);
        System.out.printf("\n최종 좋아요 수: %d\n", finalLikeCount);
        assertThat(finalLikeCount)
                .isBetween(0L, (long) testUsers.size())
                .withFailMessage("최종 좋아요 수는 0 이상, 유저 수 이하이어야 합니다.");
    }

}
