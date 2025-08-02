package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


@Tag(name = "Like V1 API", description = "좋아요(Like) API 입니다.")
public interface LikeV1ApiSpec {

    @Operation(
        summary = "상품 좋아요 등록",
        description = "상품 좋아요를 등록합니다."
    )
    ApiResponse<LikeV1Dto.LikeResponse> like(
            @Parameter(
                    name = "productId",
                    description = "상품 ID (경로 변수)",
                    required = true
            )
            @PathVariable Long productId,
            @Parameter(
                    name = "X-USER-ID",
                    description = "유저 ID (헤더)",
                    required = true
            )
            @RequestHeader("X-USER-ID") String userId
    );

    @Operation(
            summary = "상품 좋아요 취소",
            description = "상품 좋아요를 취소합니다."
    )
    ApiResponse<LikeV1Dto.LikeResponse> unLike(
            @Parameter(
                    name = "productId",
                    description = "상품 ID (경로 변수)",
                    required = true
            )
            @PathVariable Long productId,
            @Parameter(
                    name = "X-USER-ID",
                    description = "유저 ID (헤더)",
                    required = true
            )
            @RequestHeader("X-USER-ID") String userId
    );

    @Operation(
            summary = "내가 좋아요 한 상품 목록 조회",
            description = "내가 좋아요 한 상품 목록을 조회합니다."
    )
    ApiResponse<LikeV1Dto.LikeListResponse> getMyLikes(
            @Parameter(
                    name = "X-USER-ID",
                    description = "유저 ID (헤더)",
                    required = true
            )
            @RequestHeader("X-USER-ID") String userId
    );

}
