package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.domain.ranking.WeeklyRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WeeklyRankingRepositoryImpl implements WeeklyRankingRepository {

    private final WeeklyRankingJpaRepository weeklyRankingJpaRepository;

    @Override
    public List<WeeklyRanking> saveAll(List<WeeklyRanking> weeklyRankings) {
        return weeklyRankingJpaRepository.saveAll(weeklyRankings);
    }

    @Override
    public List<WeeklyRanking> findByWeekStartAndWeekEnd(LocalDate weekStart, LocalDate weekEnd) {
        return weeklyRankingJpaRepository.findByWeekStartAndWeekEnd(weekStart, weekEnd);
    }

    @Override
    public List<WeeklyRanking> findByWeekRange(LocalDate monthStart, LocalDate monthEnd) {
        return weeklyRankingJpaRepository.findByWeekRange(monthStart, monthEnd);
    }

}
