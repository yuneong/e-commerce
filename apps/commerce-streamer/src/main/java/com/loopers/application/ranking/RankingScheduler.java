package com.loopers.application.ranking;

import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingService rankingService;
    private final JobRegistry jobRegistry;
    private final JobLauncher jobLauncher;


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
     * 매주 월요일 01:00에 지난주 랭킹 집계 배치 실행
     * - 지난주 월요일 ~ 일요일
     *
     * @throws JobInstanceAlreadyCompleteException
     * @throws JobExecutionAlreadyRunningException
     * @throws JobParametersInvalidException
     * @throws JobRestartException
     * @throws NoSuchJobException
     */
    @Scheduled(cron = "0 0 1 * * MON", zone = "Asia/Seoul")
    public void weeklyRankingJob()
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, NoSuchJobException
    {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate lastWeekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastWeekEnd = today.minusWeeks(1).with(DayOfWeek.SUNDAY);

        JobParameters params = new JobParametersBuilder()
                .addString("startDate", lastWeekStart.toString())
                .addString("endDate", lastWeekEnd.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        Job job = jobRegistry.getJob("weeklyRankingJob");
        jobLauncher.run(job, params);
    }

}
