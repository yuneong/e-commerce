package com.loopers.batch.job;

import com.loopers.batch.processor.MonthlyRankingProcessor;
import com.loopers.batch.reader.MonthlyRankingReader;
import com.loopers.batch.writer.MonthlyRankingWriter;
import com.loopers.domain.ranking.WeeklyRanking;
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
public class MonthlyRankingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MonthlyRankingReader monthlyRankingReader;
    private final MonthlyRankingProcessor monthlyRankingProcessor;
    private final MonthlyRankingWriter monthlyRankingWriter;


    @Bean
    public Job monthlyRankingJob() {
        return new JobBuilder("monthlyRankingJob", jobRepository)
                .start(monthlyRankingStep())
                .build();
    }

    @Bean
    public Step monthlyRankingStep() {
        return new StepBuilder("monthlyRankingStep", jobRepository)
                .<WeeklyRanking, RankedProduct>chunk(100, transactionManager)
                .reader(monthlyRankingReader)
                .processor(monthlyRankingProcessor)
                .writer(monthlyRankingWriter)
                .build();
    }

}
