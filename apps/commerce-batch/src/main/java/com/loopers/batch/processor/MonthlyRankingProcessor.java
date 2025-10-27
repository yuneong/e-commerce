package com.loopers.batch.processor;

import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.dto.RankedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@StepScope
public class MonthlyRankingProcessor implements ItemProcessor<WeeklyRanking, RankedProduct> {

    @Value("#{jobParameters['yearMonth']}")
    private String yearMonthStr;

    @Override
    public RankedProduct process(WeeklyRanking weekly) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        // 주간 총 일수 -> 일별 점수 계산
        long totalDays = ChronoUnit.DAYS.between(weekly.getWeekStart(), weekly.getWeekEnd()) + 1; // 양 끝 날짜 포함이므로 +1
        double dailyScore = weekly.getScore() / totalDays;

        // 월과 겹치는 기간만 계산
        LocalDate effectiveStart = weekly.getWeekStart().isBefore(monthStart) ? monthStart : weekly.getWeekStart();
        LocalDate effectiveEnd = weekly.getWeekEnd().isAfter(monthEnd) ? monthEnd : weekly.getWeekEnd();

        long overlapDays = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;
        if (overlapDays <= 0) return null; // 겹치지 않으면 제외

        double monthScore = dailyScore * overlapDays;

        return new RankedProduct(weekly.getProductId(), monthScore);

    }

}
