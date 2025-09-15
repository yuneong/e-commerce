package com.loopers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductMetricsSummary {

    private Long productId;
    private Long likeCount;
    private Long stockCount;
    private Long viewCount;

}
