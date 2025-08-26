package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;


@Entity
@Getter
@Table(name = "coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    private String name;
    private CouponType type;
    private int quantity;
    private int discountValue;
    private ZonedDateTime expiredAt;

    public Coupon(String name, CouponType type, int quantity, int discountValue, ZonedDateTime expiredAt) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.discountValue = discountValue;
        this.expiredAt = expiredAt;
    }

    public int discountAmount(int itemsPrice, DiscountStrategyFactory factory) {
        return factory.create(this).discountAmount(itemsPrice);
    }

}
