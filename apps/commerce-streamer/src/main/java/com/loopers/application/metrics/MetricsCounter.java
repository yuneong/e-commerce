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

    public void addLike(int count) {
        this.likeCount += count;
    }

    public void addStock(int count) {
        this.stockCount += count;
    }

    public void addView(int count) {
        this.viewCount += count;
    }

}
