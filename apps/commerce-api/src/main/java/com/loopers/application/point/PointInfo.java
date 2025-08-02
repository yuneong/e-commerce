package com.loopers.application.point;

import com.loopers.domain.point.Point;

public record PointInfo(
        String userId,
        long balance
) {
    public static PointInfo from(Point point) {
        return new PointInfo(
                point.getUser().getUserId(),
                point.getBalance()
        );
    }
}
