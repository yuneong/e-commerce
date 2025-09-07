package com.loopers.domain.metrics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_metrics")
public class ProductMetrics {

    @EmbeddedId
    private ProductMetricsId id;

    @Column(name = "likes_delta")
    private int likesDelta;

    @Column(name = "sales_delta")
    private int salesDelta;

    @Column(name = "views_delta")
    private int viewsDelta;

    public static ProductMetrics create(Long productId, LocalDate date) {
        ProductMetrics productMetrics = new ProductMetrics();

        productMetrics.id = ProductMetricsId.create(productId, date);
        productMetrics.likesDelta = 0;
        productMetrics.salesDelta = 0;
        productMetrics.viewsDelta = 0;

        return productMetrics;
    }

    // 좋아요 수 변화 (+1/-1)
    public void increaseLike(int value) {
        this.likesDelta = Math.max(0, this.likesDelta + value);
    }

    // 판매량 변화 (+N/-N)
    public void increaseSales(int value) {
        this.salesDelta = Math.max(0, this.salesDelta + value);
    }

    // 상세 조회 수 변화 (+N)
    public void increaseView(int value) {
        this.viewsDelta = Math.max(0, this.viewsDelta + value);
    }

}
