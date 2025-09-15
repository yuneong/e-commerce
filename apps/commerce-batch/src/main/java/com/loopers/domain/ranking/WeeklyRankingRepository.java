package com.loopers.domain.ranking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyRankingRepository extends JpaRepository<WeeklyRanking, Long> {

}
