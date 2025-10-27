package com.loopers.domain.ranking;


import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface RankingRepository {

    List<WeeklyRanking> findWeeklyRankingByDate(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<WeeklyRanking> saveAllWeekly(List<WeeklyRanking> weeklyRankings);

    List<MonthlyRanking> findMonthlyRankingByDate(YearMonth monthlyPeriod, Pageable pageable);

    List<MonthlyRanking> saveAllMonthly(List<MonthlyRanking> monthlyRankings);

}
