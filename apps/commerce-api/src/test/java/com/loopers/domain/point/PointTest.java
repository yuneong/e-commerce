package com.loopers.domain.point;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PointTest {

    /**
     * - [x]  0 이하의 정수로 포인트를 충전 시 실패한다.
     */
    @DisplayName("포인트 충전 시,")
    @Nested
    class pointCharge {

        @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
        @ParameterizedTest
        @ValueSource(longs = {0L, -100L, -1L})
        void throwsIllegalArgumentException_whenChargeAmountIsZeroOrNegative(long amount) {
            // given
            User user = User.create("oyy", Gender.F, "1999-08-21", "loopers@gmail.com");
            Point point = Point.create(user);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> {
                point.charge(amount);
            });
        }

        @DisplayName("양수의 금액으로 포인트를 충전 시 성공한다.")
        @ParameterizedTest
        @ValueSource(longs = {1L, 100L, Long.MAX_VALUE})
        void success_whenChargeAmountIsPositive(long amount) {
            // given
            User user = User.create("oyy", Gender.F, "1999-08-21", "loopers@gmail.com");
            Point point = Point.create(user);

            // when
            point.charge(amount);

            // then
            assertEquals(amount, point.getBalance());
        }
    }

    @DisplayName("포인트 차감 시,")
    @Nested
    class pointUse {

        @DisplayName("보유 포인트 이하의 금액으로 차감 시 성공한다.")
        @ParameterizedTest
        @ValueSource(longs = {1L, 100L, 500L})
        void success_whenUseAmountIsValid(long amount) {
            // given
            User user = User.create("oyy", Gender.F, "1999-08-21", "loopers@gmail.com");
            Point point = Point.create(user);
            point.charge(1000L);

            // when
            point.use(amount);

            // then
            assertEquals(1000L - amount, point.getBalance());
        }

        @DisplayName("보유 포인트를 초과하여 차감 시 예외가 발생한다.")
        @Test
        void throwsException_whenUseAmountExceedsBalance() {
            // given
            User user = User.create("oyy", Gender.F, "1999-08-21", "loopers@gmail.com");
            Point point = Point.create(user);
            point.charge(1000L);

            // when & then
            assertThrows(IllegalStateException.class, () -> point.use(2000L));
        }

        @DisplayName("0 이하의 금액을 차감 시 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        void throwsIllegalArgumentException_whenUseAmountIsZeroOrNegative(long amount) {
            // given
            User user = User.create("oyy", Gender.F, "1999-08-21", "loopers@gmail.com");
            Point point = Point.create(user);
            point.charge(1000L);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> point.use(amount));
        }
    }

}
