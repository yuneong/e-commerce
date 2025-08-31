package com.loopers.domain.coupon;


public class FixedDiscountStrategy implements DiscountStrategy {

    private final int fixAmount; // 할인할 금액

    public FixedDiscountStrategy(int fixAmount) {
        if (fixAmount <= 0) {
            throw new IllegalArgumentException("할인할 금액은 0원을 넘어야 합니다.");
        }
        this.fixAmount = fixAmount;
    }

    @Override
    public int discountAmount(int totalPrice) {
        if (totalPrice < 0) {
            throw new IllegalArgumentException("총 금액은 0원 이상이어야 합니다.");
        }

        return Math.min(fixAmount, totalPrice); // 할인 금액 (총 금액보다 클 수 없음)
    }

}
