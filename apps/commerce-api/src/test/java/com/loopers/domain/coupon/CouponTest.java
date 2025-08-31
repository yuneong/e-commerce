package com.loopers.domain.coupon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    private final DiscountStrategyFactory factory = new DiscountStrategyFactory();

    @DisplayName("할인 금액 계산 성공 시,")
    @Nested
    class discountAmountSuccess {

        @DisplayName("정액 쿠폰을 사용하여 할인 금액 계산에 성공한다.")
        @Test
        void success_whenApplyFixedDiscountCoupon() {
            // given
            Coupon coupon = new Coupon("1000원 할인 쿠폰", CouponType.FIXED, 10, 1000, ZonedDateTime.now());
            int totalPrice = 5000;

            // when
            int discountAmount = coupon.discountAmount(totalPrice, factory);

            // then
            assertThat(discountAmount).isEqualTo(1000);
        }

        @DisplayName("정률 쿠폰을 사용하여 할인 금액 계산에 성공한다.")
        @Test
        void applyPercentDiscountCoupon() {
            // given
            Coupon coupon = new Coupon("10% 할인 쿠폰", CouponType.RATE, 10, 20, ZonedDateTime.now());
            int totalPrice = 10000;

            // when
            int discountAmount = coupon.discountAmount(totalPrice, factory);

            // then
            assertThat(discountAmount).isEqualTo(2000);
        }
    }

    @DisplayName("쿠폰 타입이 null이면 할인된 금액이 아닌 기본 금액으로 처리된다.")
    @Test
    void applyNoDiscountCoupon() {
        // given
        Coupon invalidCoupon = new Coupon("쿠폰 안씀", null, 10, 1000, ZonedDateTime.now());
        int totalPrice = 3000;

        // when
        int discountAmount = invalidCoupon.discountAmount(totalPrice, factory);

        // then
        assertThat(discountAmount).isEqualTo(0);
    }

    @DisplayName("총 금액이 0원 미만이면 예외가 발생한다.")
    @Test
    void throwException_whenTotalPriceBelowZero() {
        // given
        Coupon coupon = new Coupon("테스트 쿠폰", CouponType.FIXED, 10, 1000, ZonedDateTime.now());
        int totalPrice = -1000;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> coupon.discountAmount(totalPrice, factory));
    }

}
