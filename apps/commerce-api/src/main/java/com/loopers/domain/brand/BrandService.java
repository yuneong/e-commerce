package com.loopers.domain.brand;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public Brand getBrandDetail(Long brandId) {
        // repository
        return brandRepository.findById(brandId).orElseThrow(
                () -> new IllegalArgumentException("Brand not found with id: " + brandId)
        );
    }

}
