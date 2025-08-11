package com.loopers.domain.order;

import java.math.BigDecimal;


public record DiscountedOrderByCoupon (
        Long couponId,
        BigDecimal discountedTotalPrice // 최종 금액
) {

    public static DiscountedOrderByCoupon from(
            Long couponId,
            BigDecimal discountedTotalPrice
    ) {
        return new DiscountedOrderByCoupon(couponId, discountedTotalPrice);
    }

}
