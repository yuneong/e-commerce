package com.loopers.domain.order;


import lombok.Getter;

@Getter
public enum OrderStatus {

    CREATED("주문 생성"),
    PLACED("주문 접수 완료"),
    PAID("결제 완료"),
    CANCELED("주문 취소"),
    REFUNDED("환불");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}
