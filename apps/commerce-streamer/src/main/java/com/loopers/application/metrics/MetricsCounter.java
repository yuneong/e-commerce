package com.loopers.application.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MetricsCounter {

    private int likeCount;
    private int stockCount;
    private int viewCount;

    public void setLikeCount(int count) {
        this.likeCount = count;
    }

    public void setStockCount(int count) {
        this.stockCount = count;
    }

    public void setViewCount(int count) {
        this.viewCount = count;
    }

}
