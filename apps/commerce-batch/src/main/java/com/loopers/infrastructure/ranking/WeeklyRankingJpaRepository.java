package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyRankingJpaRepository extends JpaRepository<WeeklyRanking, Long> {

    List<WeeklyRanking> findByWeekStartAndWeekEnd(LocalDate weekStart, LocalDate weekEnd);

}
