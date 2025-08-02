package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandController implements BrandV1ApiSpec {

    private final BrandFacade brandFacade;

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandV1Dto.BrandResponse> getBrandDetail(
            @PathVariable Long brandId
    ) {
        // facade 호출
        BrandInfo info = brandFacade.getBrandDetail(brandId);
        // result -> response
        BrandV1Dto.BrandResponse response = BrandV1Dto.BrandResponse.from(info);

        return ApiResponse.success(response);
    }

}
