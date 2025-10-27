package com.loopers.domain.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


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

}
