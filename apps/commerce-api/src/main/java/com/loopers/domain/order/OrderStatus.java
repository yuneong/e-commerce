package com.loopers.domain.order;


import lombok.Getter;

@Getter
public enum OrderStatus {

    PENDING("주문 생성"),
    COMPLETE("주문 성공"),
    // 결제 완료???
    FAILED("주문 실패");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}
