package com.loopers.batch.job;

import com.loopers.batch.processor.WeeklyRankingProcessor;
import com.loopers.batch.reader.WeeklyRankingReader;
import com.loopers.batch.writer.WeeklyRankingWriter;
import com.loopers.dto.ProductMetricsSummary;
import com.loopers.dto.RankedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeeklyRankingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeeklyRankingReader weeklyRankingReader;
    private final WeeklyRankingProcessor weeklyRankingProcessor;
    private final WeeklyRankingWriter weeklyRankingWriter;

    @Bean
    public Job weeklyRankingJob() {
        return new JobBuilder("weeklyRankingJob", jobRepository)
                .start(weeklyRankingStep())
                .build();
    }

    @Bean
    public Step weeklyRankingStep() {
        return new StepBuilder("weeklyRankingStep", jobRepository)
                .<ProductMetricsSummary, RankedProduct>chunk(100, transactionManager)
                .reader(weeklyRankingReader)
                .processor(weeklyRankingProcessor)
                .writer(weeklyRankingWriter)
                .build();
    }

}
