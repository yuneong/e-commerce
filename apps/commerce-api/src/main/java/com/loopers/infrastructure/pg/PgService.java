package com.loopers.infrastructure.pg;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.interfaces.api.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PgService {

    private final PgClient pgClient;

    @Retry(name = "pgRequest")
    @CircuitBreaker(name = "pgRequest")
    public ApiResponse<PgV1Dto.PgResponse> callPayment(String userId, PgV1Dto.PgRequest request) {
        return pgClient.callPayment(userId, request);
    }

    @Retry(name = "pgDetail", fallbackMethod = "fallbackGetDetail")
    public ApiResponse<PgV1Dto.PgDetailResponse> getPaymentDetail(Payment payment) {
        return pgClient.getPaymentDetail(payment.getUserId(), payment.getCardDetail().getTransactionKey());
    }

    public ApiResponse<PgV1Dto.PgDetailResponse> fallbackGetDetail(Payment payment, Throwable t) {
        return ApiResponse.success(PgV1Dto.PgDetailResponse.from(
                payment,
                PaymentStatus.PENDING,
                t.getMessage() != null ? t.getMessage() : "pgDetail call failed"
        ));
    }

}
