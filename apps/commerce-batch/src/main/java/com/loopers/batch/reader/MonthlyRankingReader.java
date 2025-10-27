package com.loopers.batch.reader;

import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.domain.ranking.WeeklyRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Iterator;
import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
public class MonthlyRankingReader implements ItemReader<WeeklyRanking> {

    private final WeeklyRankingRepository weeklyRankingRepository;

    private Iterator<WeeklyRanking> iterator;

    @Value("#{jobParameters['yearMonth']}")
    private String yearMonthStr;

    @Override
    public WeeklyRanking read() {
        if (iterator == null) {
            YearMonth yearMonth = YearMonth.parse(yearMonthStr);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();

            List<WeeklyRanking> summaries = weeklyRankingRepository.findByWeekRange(monthStart, monthEnd);

            iterator = summaries.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }

}
