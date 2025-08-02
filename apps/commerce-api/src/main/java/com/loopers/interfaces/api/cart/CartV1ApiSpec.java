package com.loopers.interfaces.api.cart;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@Tag(name = "Cart V1 API", description = "장바구니(Cart) API 입니다.")
public interface CartV1ApiSpec {

    @Operation(
            summary = "장바구니 담기 요청",
            description = "장바구니에 상품을 담는 요청입니다."
    )
    ApiResponse<CartV1Dto.CartResponse> addToCart(
            @Parameter(
                    name = "request",
                    description = "장바구니에 담을 상품 정보",
                    required = true
            )
            @RequestBody CartV1Dto.CartRequest request,
            @Parameter(
                    name = "X-USER-ID",
                    description = "유저 ID (헤더)",
                    required = true
            )
            @RequestHeader("X-USER-ID") String userId
    );

    @Operation(
            summary = "장바구니 조회 요청",
            description = "장바구니에 담긴 상품들을 조회하는 요청입니다."
    )
    ApiResponse<CartV1Dto.CartItemListResponse> getMyCart(
            @Parameter(
                    name = "X-USER-ID",
                    description = "유저 ID (헤더)",
                    required = true
            )
            @RequestHeader("X-USER-ID") String userId
    );

}
