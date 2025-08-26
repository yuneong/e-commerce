package com.loopers.domain.order;

import lombok.Getter;

@Getter
public enum OrderStatus {

    DRAFT("장바구니"),
    PENDING("주문 생성 완료, 결제 대기"),
    PAID("주문 확정, 결제 완료"),
    REJECTED("주문 실패"),
    FAILED("결제 실패"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}
