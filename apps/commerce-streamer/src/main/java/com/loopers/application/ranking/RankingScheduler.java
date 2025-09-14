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
     * 어제 랭킹 점수 * 0.5 (임시로 반감기)
     */
    @Scheduled(cron = "0 50 23 * * *", zone = "Asia/Seoul")
    public void carryOverYesterdayRankings() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate tomorrow = today.plusDays(1);

        rankingService.carryOverYesterdayRankings(today, tomorrow, 0.5);
    }

}
