package com.loopers.application.payment;


import java.util.List;

/**
 * 결제 주문 정보
 * <p>
 * 결제는 주문에 대한 다수 트랜잭션으로 구성됩니다.
 *
 * @property orderId 주문 정보
 * @property transactions 주문에 엮인 트랜잭션 목록
 */

public record OrderInfo(
        Long orderId,
        List<TransactionInfo> transactions
) {

}
