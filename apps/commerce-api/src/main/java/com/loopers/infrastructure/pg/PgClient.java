package com.loopers.infrastructure.pg;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "pgClient",
        url = "${pg.url}"
)
public interface PgClient {

    // 결제 요청
    @PostMapping("/api/v1/payments")
    PgV1Dto.PgResponse callPayment(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PgV1Dto.PgRequest request
    );

    // 결제 정보 확인
    @GetMapping("/api/v1/payments/{transactionKey}")
    PgV1Dto.PgDetailResponse getPaymentDetail(
            @RequestHeader("X-USER-ID") String userId,
            @PathVariable String transactionKey
    );


    // 주문에 엮인 결제 정보 조회

}
