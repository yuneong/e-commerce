package com.loopers.domain.order;


public enum OrderStatus {

    PLACED("주문 접수"),
    PAID("결제 완료"),
    CANCELED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
