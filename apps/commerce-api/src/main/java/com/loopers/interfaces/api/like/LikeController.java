package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.LikeInfo;
import com.loopers.application.like.LikeListInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/likes")
public class LikeController implements LikeV1ApiSpec {

    private final LikeFacade likeFacade;

    @PostMapping("/products/{productId}")
    @Override
    public ApiResponse<LikeV1Dto.LikeResponse> like(
            @PathVariable Long productId,
            @RequestHeader("X-USER-ID") String userId
    ) {
        // facade
        LikeInfo info = likeFacade.like(productId, userId);
        // info -> response
        LikeV1Dto.LikeResponse response = LikeV1Dto.LikeResponse.from(info);

        return ApiResponse.success(response);
    }


    @DeleteMapping("/products/{productId}")
    @Override
    public ApiResponse<LikeV1Dto.LikeResponse>  unLike(
            @PathVariable Long productId,
            @RequestHeader("X-USER-ID") String userId
    ) {
        // facade
        LikeInfo info = likeFacade.unLike(productId, userId);
        // info -> response
        LikeV1Dto.LikeResponse response = LikeV1Dto.LikeResponse.from(info);

        return ApiResponse.success(response);
    }

//    @GetMapping("/products")
    @GetMapping("/me")
    @Override
    public ApiResponse<LikeV1Dto.LikeListResponse> getMyLikes(@RequestHeader("X-USER-ID") String userId) {
        // facade
        LikeListInfo info = likeFacade.getLikeProducts(userId);
        // info -> response
        LikeV1Dto.LikeListResponse response = LikeV1Dto.LikeListResponse.from(info);

        return ApiResponse.success(response);
    }

}
