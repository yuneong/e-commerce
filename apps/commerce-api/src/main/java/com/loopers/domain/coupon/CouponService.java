package com.loopers.domain.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final DiscountStrategyFactory discountStrategyFactory;

    public int calculateDiscountAmount(String userId, Long couponId, int itemsPrice) {
        // 쿠폰 없음 → 0원 할인
        if (couponId == null) {
            return discountStrategyFactory.create(null).discountAmount(itemsPrice);
        }

        // 존재/보유 검증
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않습니다."));
        UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 보유한 쿠폰이 아닙니다."));

        // 상태/만료 검증
        userCoupon.validate();

        return coupon.discountAmount(itemsPrice, discountStrategyFactory);
    }

    @Transactional
    public void restoreUserCoupon(String userId, Long couponId) {
        UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 보유한 쿠폰이 아닙니다."));

        userCoupon.restoreCoupon();

        userCouponRepository.save(userCoupon);
    }
}
