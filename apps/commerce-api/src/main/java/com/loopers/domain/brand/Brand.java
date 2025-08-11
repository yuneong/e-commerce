package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "brands")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseEntity {

    private String name;
    private String description;
    private String imageUrl;

    public static Brand create(String name, String description, String imageUrl) {
        Brand brand = new Brand();

        brand.validate(name, imageUrl);

        brand.name = name;
        brand.description = description;
        brand.imageUrl = imageUrl;

        return brand;
    }

    private void validate(String name, String imageUrl) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("브랜드명은 필수입니다.");
        if (!name.matches("^[가-힣a-zA-Z0-9]+$"))
            throw new IllegalArgumentException("브랜드명은 한글, 영어, 숫자만 허용됩니다.");

        if (imageUrl == null || imageUrl.isBlank())
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        if (!imageUrl.matches("^https?://.*\\.(jpg|jpeg|png|gif|webp|svg)$"))
            throw new IllegalArgumentException("유효한 이미지 확장자를 가진 URL이어야 합니다.");
    }

}
