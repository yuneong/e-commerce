package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.DiscountedOrderByCoupon;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;


@Entity
@Getter
@Table(name = "coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    private String name;
    private CouponType type;
    private int quantity;
    private int discountValue; // 할인 금액 (고정 금액 할인 또는 비율 할인에 따라 다름)
    private ZonedDateTime expiredAt;

    public Coupon(String name, CouponType type, int quantity, int discountValue, ZonedDateTime expiredAt) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.discountValue = discountValue;
        this.expiredAt = expiredAt;
    }

    public DiscountedOrderByCoupon applyDiscount(BigDecimal totalPrice, DiscountStrategyFactory factory) {
        BigDecimal discountedTotalPrice = factory.create(this).applyDiscount(totalPrice);

        return DiscountedOrderByCoupon.from(this.getId(), discountedTotalPrice);
    }

}
