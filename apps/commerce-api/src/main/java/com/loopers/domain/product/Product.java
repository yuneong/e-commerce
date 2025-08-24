package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    private String name;

    private String description;

    private String imageUrl;

    private int price;

    private int stock;

    private ProductStatus status = ProductStatus.ACTIVE; // 기본 상태를 ACTIVE로 설정`

    private Long likeCount = 0L;

    @Version
    private Long version;

    public static Product create(Brand brand, String name, String description, String imageUrl, int price, int stock) {
        Product product = new Product();

        product.validate(brand, name, imageUrl, price, stock);

        product.brand = brand;
        product.name = name;
        product.description = description;
        product.imageUrl = imageUrl;
        product.price = price;
        product.stock = stock;

        return product;
    }

    private void validate(Brand brand, String name, String imageUrl, int price, int stock) {
        if (brand == null) throw new IllegalArgumentException("브랜드는 필수입니다.");

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("상품명은 필수입니다.");
        if (!name.matches("^[가-힣a-zA-Z0-9]+$"))
            throw new IllegalArgumentException("상품명은 한글, 영어, 숫자만 허용됩니다.");

        if (imageUrl == null || imageUrl.isBlank())
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        if (!imageUrl.matches("^https?://.*\\.(jpg|jpeg|png|gif|webp|svg)$"))
            throw new IllegalArgumentException("유효한 이미지 확장자를 가진 URL이어야 합니다.");

        if (price < 0)
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");

        if (stock <= 0)
            throw new IllegalArgumentException("재고는 0 초과이어야 합니다.");
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감할 재고 수량은 0보다 커야 합니다.");
        }
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }

    public void increaseLikeCount() {
        this.likeCount += 1L;
    }

    public void decreaseLikeCount() {
        if (this.likeCount <= 0L) {
            throw new IllegalStateException("Like count cannot be less than zero.");
        }
        this.likeCount -= 1L;
    }

    public void restoreStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("복원할 재고 수량은 0보다 커야 합니다.");
        }
        this.stock += quantity;
    }

}
