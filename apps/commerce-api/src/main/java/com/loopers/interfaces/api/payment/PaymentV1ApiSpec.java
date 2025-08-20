package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "Payment V1 API", description = "결제(Payment) API 입니다.")
public interface PaymentV1ApiSpec {

    @Operation(
        summary = "결제 요청",
        description = "주문 생성이 성공된 후 결제를 요청합니다."
    )
    ApiResponse<PaymentV1Dto.ProcessResponse> processPayment(
            @Parameter(
                    name = "request",
                    description = "결제 정보",
                    required = true
            )
            @RequestBody PaymentV1Dto.ProcessRequest request
    );

    @Operation(
            summary = "외부 결제 시스템 콜백",
            description = "외부 결제 요청에 대한 콜백을 처리합니다."
    )
    ApiResponse<Object> paymentCallback(
            @Parameter(
                    name = "request",
                    description = "외부 결제 요청 정보",
                    required = true
            )
            @RequestBody PaymentV1Dto.CallbackRequest request
    );

}
