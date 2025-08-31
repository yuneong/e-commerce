package com.loopers.domain.coupon;


public class RateDiscountStrategy implements DiscountStrategy {

    private final int rate; // 할인 비율 (1~100)

    public RateDiscountStrategy(int rate) {
        if (rate < 1 || rate > 100) {
            throw new IllegalArgumentException("1~100 사이의 퍼센트만 입력 가능합니다.");
        }
        this.rate = rate;
    }

    @Override
    public int discountAmount(int totalPrice) {
        if (totalPrice < 0) {
            throw new IllegalArgumentException("총 금액은 0원 이상이어야 합니다.");
        }

        return totalPrice * rate / 100; // 할인 금액 (계산 방식 - 내림)
    }

}
