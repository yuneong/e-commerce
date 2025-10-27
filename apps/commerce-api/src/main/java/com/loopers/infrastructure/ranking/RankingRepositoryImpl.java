package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MonthlyRanking;
import com.loopers.domain.ranking.RankingRepository;
import com.loopers.domain.ranking.WeeklyRanking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RankingRepositoryImpl implements RankingRepository {

    private final WeeklyRankingJpaRepository weeklyRankingJpaRepository;
    private final MonthlyRankingJpaRepository monthlyRankingJpaRepository;


    @Override
    public List<WeeklyRanking> findWeeklyRankingByDate(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return weeklyRankingJpaRepository.findByWeekStartAndWeekEnd(startDate, endDate, pageable);
    }

    @Override
    public List<WeeklyRanking> saveAllWeekly(List<WeeklyRanking> weeklyRankings) {
        return weeklyRankingJpaRepository.saveAll(weeklyRankings);
    }

    @Override
    public List<MonthlyRanking> findMonthlyRankingByDate(YearMonth monthlyPeriod, Pageable pageable) {
        return monthlyRankingJpaRepository.findByMonthPeriod(monthlyPeriod, pageable);
    }

    @Override
    public List<MonthlyRanking> saveAllMonthly(List<MonthlyRanking> monthlyRankings) {
        return monthlyRankingJpaRepository.saveAll(monthlyRankings);
    }
}
