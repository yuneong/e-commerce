package com.loopers.domain.coupon;

import java.math.BigDecimal;

public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal applyDiscount(BigDecimal totalPrice) {
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("총 금액은 0원 이상이어야 합니다");
        }
        return totalPrice;
    }

}
