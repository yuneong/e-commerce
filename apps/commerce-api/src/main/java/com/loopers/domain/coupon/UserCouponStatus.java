package com.loopers.domain.coupon;

public enum UserCouponStatus {

    AVAILABLE("사용가능"),
    USED("사용"),
    EXPIRED("만료");

    private final String description;

    UserCouponStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}

