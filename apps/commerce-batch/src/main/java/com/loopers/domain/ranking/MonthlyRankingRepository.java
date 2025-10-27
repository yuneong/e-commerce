package com.loopers.domain.ranking;

import java.util.List;

public interface MonthlyRankingRepository {

    List<MonthlyRanking> saveAll(List<MonthlyRanking> monthlyRankings);

}
