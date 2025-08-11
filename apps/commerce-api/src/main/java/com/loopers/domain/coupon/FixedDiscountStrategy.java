package com.loopers.domain.coupon;

import java.math.BigDecimal;

public class FixedDiscountStrategy implements DiscountStrategy {

    private final int fixAmount; // 할인할 금액

    public FixedDiscountStrategy(int fixAmount) {
        if (fixAmount <= 0) {
            throw new IllegalArgumentException("할인할 금액은 0원을 넘어야 합니다");
        }
        this.fixAmount = fixAmount;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal totalPrice) {
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("총 금액은 0원 이상이어야 합니다");
        }

        BigDecimal discount = BigDecimal.valueOf(fixAmount);
        return totalPrice.compareTo(discount) >= 0 ? totalPrice.subtract(discount) : BigDecimal.ZERO;
    }

}
