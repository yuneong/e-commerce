package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyRankingJpaRepository extends JpaRepository<WeeklyRanking, Long> {

    List<WeeklyRanking> findByWeekStartAndWeekEnd(LocalDate weekStart, LocalDate weekEnd);

    @Query("""
        SELECT w
        FROM WeeklyRanking w
        WHERE w.weekStart <= :monthEnd
          AND w.weekEnd   >= :monthStart
    """)
    List<WeeklyRanking> findByWeekRange(@Param("monthStart") LocalDate monthStart, @Param("monthEnd") LocalDate monthEnd);

}
