package com.loopers.domain.coupon;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.TestFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CouponServiceIntegrationTest {

    @Autowired
    private CouponService couponService;

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

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("할인 금액 계산 시,")
    @Nested
    class calculateDiscountAmount {
        @Test
        @DisplayName("쿠폰 없이 주문할 경우 할인 금액은 0원이다.")
        void success_whenWithoutCoupon() {
            // given
            String userId = "user123";
            Brand brand = brandRepository.save(TestFixture.createBrand());
            Product product = productRepository.save(TestFixture.createProduct(brand));
            List<OrderItem> items = TestFixture.createOrderItems(product, 2);
            int itemsPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();

            // when
            int discountAmount = couponService.calculateDiscountAmount(userId, null, itemsPrice);

            // then
            assertThat(discountAmount).isEqualTo(0);
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 사용 시 예외가 발생한다.")
        void throwsException_whenInvalidCouponId() {
            // given
            String userId = "user123";
            Long invalidCouponId = 999L;
            Brand brand = brandRepository.save(TestFixture.createBrand());
            Product product = productRepository.save(TestFixture.createProduct(brand));
            List<OrderItem> items = TestFixture.createOrderItems(product, 2);
            int itemsPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    couponService.calculateDiscountAmount(userId, invalidCouponId, itemsPrice));
        }

        @Test
        @DisplayName("유저가 보유하지 않은 쿠폰 사용 시 예외가 발생한다.")
        void throwsException_whenNotOwnedCoupon() {
            // given
            Coupon coupon = couponRepository.save(new Coupon("10% 할인 쿠폰", CouponType.RATE, 10, 10, ZonedDateTime.now()));
            String userId = "user123";
            Brand brand = brandRepository.save(TestFixture.createBrand());
            Product product = productRepository.save(TestFixture.createProduct(brand));
            List<OrderItem> items = TestFixture.createOrderItems(product, 1);
            int itemsPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    couponService.calculateDiscountAmount(userId, coupon.getId(), itemsPrice));
        }

        @Test
        @DisplayName("사용할 수 없는 상태의 쿠폰 사용 시 예외가 발생한다.")
        void throwsException_whenInvalidStatus() {
            // given
            String userId = "user123";
            Coupon coupon = couponRepository.save(new Coupon("1000원 할인 쿠폰", CouponType.FIXED, 10, 1000, ZonedDateTime.now()));
            userCouponRepository.save(new UserCoupon(userId, coupon.getId(), UserCouponStatus.USED, ZonedDateTime.now().minusMinutes(1), ZonedDateTime.now().plusDays(2)));
            Brand brand = brandRepository.save(TestFixture.createBrand());
            Product product = productRepository.save(TestFixture.createProduct(brand));
            List<OrderItem> items = TestFixture.createOrderItems(product, 1);
            int itemsPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();

            // when & then
            assertThrows(IllegalStateException.class, () ->
                    couponService.calculateDiscountAmount(userId, coupon.getId(), itemsPrice));
        }

        @Test
        @DisplayName("정상 쿠폰 사용 시 할인 금액 계산에 성공한다.")
        void success_whenValidCoupon() {
            // given
            String userId = "user123";
            Coupon coupon = couponRepository.save(new Coupon("200원 할인 쿠폰", CouponType.FIXED, 10, 200, ZonedDateTime.now()));
            userCouponRepository.save(UserCoupon.create(userId, coupon.getId(), ZonedDateTime.now().plusDays(2)));
            Brand brand = brandRepository.save(TestFixture.createBrand());
            Product product = productRepository.save(TestFixture.createProduct(brand));
            List<OrderItem> items = TestFixture.createOrderItems(product, 1); // totalPrice = 1000
            int itemsPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();

            // when
            int discountAmount = couponService.calculateDiscountAmount(userId, coupon.getId(), itemsPrice);

            // then
            assertAll(
                    () -> assertThat(discountAmount).isEqualTo(200),
                    () -> assertThat(itemsPrice - discountAmount).isEqualTo(800)
            );
        }
    }

}
