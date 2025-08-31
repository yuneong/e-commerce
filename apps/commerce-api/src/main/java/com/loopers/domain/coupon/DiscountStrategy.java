package com.loopers.domain.coupon;


public interface DiscountStrategy {

    int discountAmount(int totalPrice);

}
