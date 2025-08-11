package com.loopers.domain.coupon;

import com.loopers.domain.order.DiscountedOrderByCoupon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    private final DiscountStrategyFactory factory = new DiscountStrategyFactory();

    @DisplayName("할인 적용 성공 시,")
    @Nested
    class ApplyDiscountSuccess {

        @DisplayName("정액 쿠폰이 정상적으로 적용된다.")
        @Test
        void applyFixedDiscountCoupon() {
            // given
            Coupon coupon = new Coupon("1000원 할인 쿠폰", CouponType.FIXED, 10, 1000, ZonedDateTime.now());
            BigDecimal totalPrice = BigDecimal.valueOf(5000);

            // when
            DiscountedOrderByCoupon discounted = coupon.applyDiscount(totalPrice, factory);

            // then
            assertThat(discounted.discountedTotalPrice()).isEqualTo(BigDecimal.valueOf(4000));
        }

        @DisplayName("정률 쿠폰이 정상적으로 적용된다.")
        @Test
        void applyPercentDiscountCoupon() {
            // given
            Coupon coupon = new Coupon("10% 할인 쿠폰", CouponType.RATE, 10, 10, ZonedDateTime.now());
            BigDecimal totalPrice = BigDecimal.valueOf(10000);

            // when
            DiscountedOrderByCoupon discounted = coupon.applyDiscount(totalPrice, factory);

            // then
            assertThat(discounted.discountedTotalPrice()).isEqualTo(BigDecimal.valueOf(9000));
        }
    }

    @DisplayName("쿠폰 타입이 null이면 할인된 금액이 아닌 기본 금액으로 처리된다.")
    @Test
    void applyNoDiscountCoupon() {
        // given
        Coupon invalidCoupon = new Coupon("쿠폰 안씀", null, 10, 1000, ZonedDateTime.now());
        BigDecimal totalPrice = BigDecimal.valueOf(3000);

        // when
        DiscountedOrderByCoupon discounted = invalidCoupon.applyDiscount(totalPrice, factory);

        // then
        assertThat(discounted.discountedTotalPrice()).isEqualTo(BigDecimal.valueOf(3000));
    }

    @DisplayName("총 금액이 0원 미만이면 예외가 발생한다.")
    @Test
    void throwException_whenTotalPriceBelowZero() {
        // given
        Coupon coupon = new Coupon("테스트 쿠폰", CouponType.FIXED, 10, 1000, ZonedDateTime.now());
        BigDecimal totalPrice = BigDecimal.valueOf(-1000);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> coupon.applyDiscount(totalPrice, factory));
    }

}
