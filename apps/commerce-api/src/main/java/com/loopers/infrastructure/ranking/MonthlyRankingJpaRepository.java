package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MonthlyRanking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.YearMonth;
import java.util.List;


public interface MonthlyRankingJpaRepository extends JpaRepository<MonthlyRanking, Long> {

    @Query("""
        SELECT mr
        FROM MonthlyRanking mr
        WHERE mr.monthPeriod = :monthPeriod
        ORDER BY mr.rank ASC
    """)
    List<MonthlyRanking> findByMonthPeriod(
            @Param("monthlyPeriod") YearMonth monthPeriod,
            Pageable pageable
    );
}
