package com.loopers.domain.metrics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetricsId implements Serializable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "date")
    private LocalDate date;

    public static ProductMetricsId create(
            Long productId,
            LocalDate date
    ) {
        ProductMetricsId id = new ProductMetricsId();

        id.productId = productId;
        id.date = date;

        return id;
    }

}
