package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@Tag(name = "Order V1 API", description = "주문(Order) API 입니다.")
public interface OrderV1ApiSpec {

    @Operation(
        summary = "주문 요청",
        description = "상품에 대한 주문을 생성을 처리합니다. (상품 차감/쿠폰 사용 포함)"
    )
    ApiResponse<OrderV1Dto.OrderResponse> placeOrder(
            @Parameter(
                    name = "request",
                    description = "주문 정보",
                    required = true
            )
            @RequestBody OrderV1Dto.OrderRequest request,
            @Parameter(
                    name = "X-USER-ID",
                    description = "유저 ID (헤더)",
                    required = true
            )
            @RequestHeader("X-USER-ID") String userId
    );

    @Operation(
            summary = "유저의 주문 목록 조회",
            description = "유저의 주문 목록을 조회합니다."
    )
    ApiResponse<OrderV1Dto.OrderListResponse> getOrders(
            @Parameter(
                    name = "X-USER-ID",
                    description = "유저 ID (헤더)",
                    required = true
            )
            @RequestHeader("X-USER-ID") String userId
    );

    @Operation(
            summary = "단일 주문 상세 조회",
            description = "주문 ID를 통해 단일 주문의 상세 정보를 조회합니다."
    )
    ApiResponse<OrderV1Dto.OrderResponse> getOrder(
            @Parameter(
                    name = "orderId",
                    description = "주문 ID (경로 변수)",
                    required = true
            )
            @PathVariable Long orderId,
            @Parameter(
                    name = "X-USER-ID",
                    description = "유저 ID (헤더)",
                    required = true
            )
            @RequestHeader("X-USER-ID") String userId
    );

}
