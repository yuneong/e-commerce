package com.loopers.domain.coupon;


public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public int discountAmount(int totalPrice) {
        if (totalPrice < 0) {
            throw new IllegalArgumentException("총 금액은 0원 이상이어야 합니다.");
        }
        return 0;
    }

}
