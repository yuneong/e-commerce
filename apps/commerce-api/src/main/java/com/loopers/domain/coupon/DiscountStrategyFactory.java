package com.loopers.domain.coupon;

import org.springframework.stereotype.Component;

@Component
public class DiscountStrategyFactory {

    public DiscountStrategy create(Coupon coupon) {
        if (coupon == null || coupon.getType() == null) {
            return new NoDiscountStrategy();
        }

        return switch (coupon.getType()) {
            case FIXED -> new FixedDiscountStrategy(coupon.getDiscountValue());
            case RATE -> new RateDiscountStrategy(coupon.getDiscountValue());
        };
    }

}
