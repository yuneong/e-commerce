package com.loopers.application.brand;


import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;

    public BrandInfo getBrandDetail(Long brandId) {
        // service
        Brand brand = brandService.getBrandDetail(brandId);
        // domain -> result
        return BrandInfo.from(brand);
    }

}
