package com.loopers.domain.ranking;

import java.util.List;

public interface WeeklyRankingRepository {

    List<WeeklyRanking> saveAll(List<WeeklyRanking> weeklyRankings);

}
