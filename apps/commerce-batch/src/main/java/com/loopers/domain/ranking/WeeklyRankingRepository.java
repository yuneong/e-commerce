package com.loopers.domain.ranking;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyRankingRepository {

    List<WeeklyRanking> saveAll(List<WeeklyRanking> weeklyRankings);

    List<WeeklyRanking> findByWeekStartAndWeekEnd(LocalDate weekStart, LocalDate weekEnd);

    List<WeeklyRanking> findByWeekRange(LocalDate monthStart, LocalDate monthEnd);

}
