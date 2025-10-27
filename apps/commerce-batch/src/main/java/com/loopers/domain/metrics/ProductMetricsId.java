package com.loopers.domain.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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

}
