package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Getter
@Table(name = "user_coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

    private String userId;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "coupon_id")
//    private Coupon coupon;
    private Long couponId;
    private UserCouponStatus status = UserCouponStatus.AVAILABLE;
    private ZonedDateTime usedAt;
    private ZonedDateTime expiredAt; // 만료일
    @Version private Long version;

    public UserCoupon(String userId, Long couponId, UserCouponStatus status, ZonedDateTime usedAt, ZonedDateTime expiredAt) {
        this.userId = userId;
        this.couponId = couponId;
        this.status = status;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }

    public static UserCoupon create(String userId, Long couponId, ZonedDateTime expiredAt) {
        UserCoupon userCoupon = new UserCoupon();

        userCoupon.userId = userId;
        userCoupon.couponId = couponId;
        userCoupon.expiredAt = expiredAt;

        return userCoupon;
    }

    public void use() {
        validate();

        this.status = UserCouponStatus.USED;
        this.usedAt = ZonedDateTime.now();
    }

    public void validate() {
        if (status != UserCouponStatus.AVAILABLE) {
            throw new IllegalStateException("이미 사용한 쿠폰입니다.");
        }
        if (expiredAt != null && expiredAt.isBefore(ZonedDateTime.now())) {
            throw new IllegalStateException("쿠폰이 만료되었습니다.");
        }
    }

}
