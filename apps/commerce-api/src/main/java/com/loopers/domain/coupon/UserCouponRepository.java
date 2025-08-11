package com.loopers.domain.coupon;


import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCoupon> findByUserIdAndCouponId(String userId, Long couponId);

    UserCoupon save(UserCoupon userCoupon);

}
