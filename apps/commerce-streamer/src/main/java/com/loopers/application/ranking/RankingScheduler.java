package com.loopers.application.ranking;

import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingService rankingService;
    private final RestTemplate restTemplate = new RestTemplate();

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

    /**
     * 주간 랭킹 배치
     * 매주 월요일 01:00에 지난주 랭킹 집계 배치 api 호출
     * - 지난주 월요일 ~ 일요일
     */
    @Scheduled(cron = "0 0 1 * * MON", zone = "Asia/Seoul")
    public void weeklyRankingJob() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate lastWeekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastWeekEnd = today.minusWeeks(1).with(DayOfWeek.SUNDAY);

        String url = String.format(
                "http://localhost:28081/api/v1/batch/weekly-ranking?startDate=%s&endDate=%s",
                lastWeekStart,
                lastWeekEnd
        );

        restTemplate.postForEntity(url, null, String.class);
    }

}
