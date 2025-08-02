package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductCommand;
import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductListInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductV1ApiSpec{

    private final ProductFacade productFacade;

    @GetMapping("")
    @Override
    public ApiResponse<ProductV1Dto.ProductListResponse> getProducts(
            @RequestParam (required = false) Long brandId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestHeader("X-USER-ID") String userId
    ) {
        // command 생성
        ProductCommand command = ProductCommand.of(brandId, pageable);
        // facade 호출
        ProductListInfo productListInfo = productFacade.getProducts(command, userId);
        // result -> response
        ProductV1Dto.ProductListResponse response = ProductV1Dto.ProductListResponse.from(productListInfo);

        return ApiResponse.success(response);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.ProductDetailResponse> getProductDetail(
            @PathVariable Long productId,
            @RequestHeader("X-USER-ID") String userId
    ) {
        // facade 호출
        ProductInfo productInfo = productFacade.getProductDetail(productId, userId);
        // result -> response
        ProductV1Dto.ProductDetailResponse response = ProductV1Dto.ProductDetailResponse.from(productInfo);

        return ApiResponse.success(response);
    }

}
