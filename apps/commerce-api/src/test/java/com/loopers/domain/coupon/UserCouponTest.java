package com.loopers.domain.coupon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserCouponTest {

    @Test
    @DisplayName("정상 상태의 쿠폰은 사용될 수 있다.")
    void useAvailableCoupon() {
        // given
        UserCoupon userCoupon = UserCoupon.create("oyy", 1L, ZonedDateTime.now().plusDays(1));

        // when
        userCoupon.use();

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
        assertThat(userCoupon.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 사용된 쿠폰은 다시 사용할 수 없다.")
    void throwException_whenAlreadyUsed() {
        // given
        UserCoupon userCoupon = new UserCoupon(
                "oyy",
                1L,
                UserCouponStatus.USED,
                ZonedDateTime.now().minusMinutes(1),
                ZonedDateTime.now().plusDays(2)
        );

        // when & then
        assertThrows(IllegalStateException.class, userCoupon::use);
    }

    @Test
    @DisplayName("만료된 쿠폰은 사용할 수 없다.")
    void throwException_whenExpired() {
        // given
        UserCoupon userCoupon = new UserCoupon(
                "oyy",
                1L,
                UserCouponStatus.USED,
                null,
                ZonedDateTime.now().minusMinutes(1)
        );

        // when & then
        assertThrows(IllegalStateException.class, userCoupon::use);
    }

}
