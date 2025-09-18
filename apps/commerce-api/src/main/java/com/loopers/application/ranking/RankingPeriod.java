package com.loopers.application.ranking;

import java.util.Locale;

public enum RankingPeriod {
    DAILY,
    WEEKLY,
    MONTHLY;

    public static RankingPeriod fromString(String period) {
        if (period == null) return DAILY;

        try {
            return RankingPeriod.valueOf(period.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 RankingPeriod: " + period);
        }

    }
}
