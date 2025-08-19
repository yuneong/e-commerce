package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.*;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@DisplayName("동시성 테스트")
class OrderFacadeConcurrencyTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private List<User> users;
    private Product product;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        users = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String userId = "user" + i;
            String email = "user" + i + "@loopers.com";

            User user = userRepository.save(User.create(userId, Gender.F, "1999-08-21", email));
            users.add(user);
        }

        Brand brand = brandRepository.save(TestFixture.createBrand());
        product = productRepository.save(TestFixture.createProduct(brand));

        coupon = couponRepository.save(
                new Coupon("200원 쿠폰", CouponType.FIXED, 10, 200, ZonedDateTime.now().plusDays(1))
        );

        userCouponRepository.save(
                UserCoupon.create(users.get(0).getUserId(), coupon.getId(), coupon.getExpiredAt())
        );
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }

    private OrderCommand createOrderCommand(String userId, Long productId, Long couponId) {
        return new OrderCommand(
                userId,
                List.of(new OrderItemCommand(productId, 1, 1000)),
                couponId
        );
    }

    @DisplayName("동일한 쿠폰을 여러 기기에서 동시에 주문해도 단 1회만 사용된다. (낙관적 락)")
    @Test
    void coupon_should_be_used_only_once_concurrently() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        String userId = users.get(0).getUserId();
        Long productId = product.getId();
        Long couponId = coupon.getId();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    OrderCommand command = createOrderCommand(userId, productId, couponId);
                    orderFacade.placeOrder(command);

                    Optional<UserCoupon> userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId);
                    System.out.printf("🎫[%s] 사용 쿠폰 아이디: %d%n", Thread.currentThread().getName(), userCoupon.get().getCouponId());
                } catch (Exception e) {
                    System.out.printf("🎫[%s] 쿠폰 사용 실패: %s%n", Thread.currentThread().getName(), e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        List<OrderInfo> orders = orderFacade.getOrders(userId);
        assertThat(orders).isNotEmpty();
        assertThat(orders.size()).isLessThanOrEqualTo(threadCount);

        long couponUsedCount = orders.stream()
                .filter(o -> o.couponId() != null && o.couponId().equals(couponId))
                .count();
        assertThat(couponUsedCount).isEqualTo(1); // 정확히 한 번만 사용되어야 함
    }

    @DisplayName("동일 상품에 대해 동시에 여러 주문을 해도 재고는 정확히 차감된다. (비관적 락)")
    @Test
    void stock_should_be_deducted_correctly_concurrently() throws InterruptedException {
        int threadCount = users.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Long productId = product.getId();

        for (int i = 0; i < threadCount; i++) {
            String userId = users.get(i).getUserId();
            executor.submit(() -> {
                try {
                    OrderCommand command = createOrderCommand(userId, productId, null); // 쿠폰 없음
                    orderFacade.placeOrder(command);

                    Product product = productRepository.findById(productId).orElseThrow();
                    System.out.printf("🛍️[%s] 현재 상품 재고: %d%n", Thread.currentThread().getName(), product.getStock());

                } catch (Exception e) {
                    System.out.println("🛍️재고 차감 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Product product = productRepository.findById(productId).orElseThrow();

        assertAll(
                () -> assertThat(product.getStock()).isEqualTo(5),
                () -> assertThat(product.getStock()).isGreaterThanOrEqualTo(0),
                () -> assertThat(product.getStock()).isLessThanOrEqualTo(10),
                () -> assertThat(product.getId()).isEqualTo(productId)
        );
    }

}
