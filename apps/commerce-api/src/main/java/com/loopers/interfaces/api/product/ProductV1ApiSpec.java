package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Product V1 API", description = "상품(Product) API 입니다.")
public interface ProductV1ApiSpec {

    @Operation(
        summary = "상품 목록 조회",
        description = "상품 목록을 조회합니다."
    )
    ApiResponse<ProductV1Dto.ProductListResponse> getProducts(
            @Parameter(
                    name = "brandId",
                    description = "브랜드 ID (쿼리 파라미터)",
                    required = false
            )
            @RequestParam Long brandId,
            @Parameter(
                    name = "pageable",
                    description = "페이징 정보 (쿼리 파라미터)",
                    required = false
            )
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @Parameter(
                    name = "X-USER-ID",
                    description = "유저 ID (헤더)",
                    required = false
            )
            @RequestHeader("X-USER-ID") String userId
    );

    @Operation(
            summary = "상품 정보 조회",
            description = "상품 ID로 상품 정보를 조회합니다."
    )
    ApiResponse<ProductV1Dto.ProductDetailResponse> getProductDetail(
            @Parameter(
                    name = "productId",
                    description = "상품 ID (경로 변수)",
                    required = true
            )
            @PathVariable Long productId,
            @Parameter(
                name = "X-USER-ID",
                description = "유저 ID (헤더)",
                required = false
            )
            @RequestHeader("X-USER-ID") String userId
    );

}
