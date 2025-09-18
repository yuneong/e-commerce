package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyRanking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface WeeklyRankingJpaRepository extends JpaRepository<WeeklyRanking, Long> {

    @Query("""
        SELECT wr
        FROM WeeklyRanking wr
        WHERE wr.weekStart = :weekStart
          AND wr.weekEnd = :weekEnd
        ORDER BY wr.rank ASC
    """)
    List<WeeklyRanking> findByWeekStartAndWeekEnd(
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd,
            Pageable pageable
    );

}
