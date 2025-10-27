package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MonthlyRanking;
import com.loopers.domain.ranking.MonthlyRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyRankingRepositoryImpl implements MonthlyRankingRepository {

    private final MonthlyRankingJpaRepository monthlyRankingJpaRepository;

    @Override
    public List<MonthlyRanking> saveAll(List<MonthlyRanking> monthlyRankings) {
        return monthlyRankingJpaRepository.saveAll(monthlyRankings);
    }

}
