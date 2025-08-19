package com.loopers.infrastructure.pg;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "pgClient",
        url = "${pg.url}"
)
public interface PgClient {

    // 결제 요청
    @PostMapping("/api/v1/payments")
    PgResponse callPayment(@RequestHeader("X-USER-ID") String userId, @RequestBody PgRequest request);

    // 결제 정보 확인

    // 주문에 엮인 결제 정보 조회

}
