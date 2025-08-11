package com.loopers.domain.coupon;

import com.loopers.domain.order.DiscountedOrderByCoupon;
import com.loopers.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final DiscountStrategyFactory discountStrategyFactory;

    @Transactional
    public DiscountedOrderByCoupon useCoupon(String userId, Long couponId, List<OrderItem> items) {
        // 쿠폰 적용 전 총 금액
        int totalPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();

        // 쿠폰 사용 X
        if (couponId == null) {
            return DiscountedOrderByCoupon.from(
                    null,
                    discountStrategyFactory.create(null).applyDiscount(BigDecimal.valueOf(totalPrice))
            );
        }

        // 쿠폰 검증
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않습니다."));

        UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 보유한 쿠폰이 아닙니다."));

        if (userCoupon.getStatus() != UserCouponStatus.AVAILABLE) {
            throw new IllegalStateException("사용할 수 없는 쿠폰입니다.");
        }

        // 쿠폰 적용
        DiscountedOrderByCoupon discountedOrderByCoupon = coupon.applyDiscount(BigDecimal.valueOf(totalPrice), discountStrategyFactory);

        // 쿠폰 사용 처리
        userCoupon.use();

        return discountedOrderByCoupon;
    }
}
