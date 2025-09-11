package com.loopers.application.ranking;

import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingService rankingService;

    /**
     * 매일 23:50에 어제 랭킹을 오늘 랭킹으로 가중치 낮춰 이관
     * 어제 랭킹 점수 * 0.8
     */
    @Scheduled(cron = "0 50 23 * * *", zone = "Asia/Seoul")
    public void carryOverYesterdayRankings() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        rankingService.carryOverYesterdayRankings(yesterday, today, 0.8);
    }

}
