package com.loopers.job.tasklet;

import com.loopers.domain.metrics.ProductMetricsRepository;
import com.loopers.domain.ranking.RankingCalculator;
import com.loopers.dto.ProductMetricsSummary;
import com.loopers.dto.RankedProduct;
import com.loopers.job.writer.WeeklyRankingWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WeeklyRankingTasklet implements Tasklet {

    private final ProductMetricsRepository productMetricsRepository;
    private final RankingCalculator rankingCalculator;
    private final WeeklyRankingWriter weeklyRankingWriter;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        JobParameters params = contribution.getStepExecution().getJobParameters();
        LocalDate startDate = LocalDate.parse(Objects.requireNonNull(params.getString("startDate")));
        LocalDate endDate = LocalDate.parse(Objects.requireNonNull(params.getString("endDate")));

        // Reader : 집계 데이터 조회
        List<ProductMetricsSummary> summaries = productMetricsRepository.findByIdMetricsDateBetween(startDate, endDate);

        // Processor : 계산
        List<RankedProduct> rankedProducts = summaries.stream()
                .map(rankingCalculator::weightedSum)
                .toList();

        // Writer : MV
        weeklyRankingWriter.saveTop100(rankedProducts, startDate, endDate);

        return RepeatStatus.FINISHED;
    }
}
