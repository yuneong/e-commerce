package com.loopers.job.writer;

import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.domain.ranking.WeeklyRankingRepository;
import com.loopers.dto.RankedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class WeeklyRankingWriter {

    private final WeeklyRankingRepository weeklyRankingRepository;

    public void saveTop100(List<RankedProduct> rankedProducts, LocalDate startDate, LocalDate endDate) {
        List<RankedProduct> top100 = rankedProducts.stream()
                .sorted(Comparator.comparingDouble(RankedProduct::score).reversed())
                .limit(100)
                .toList();

        List<WeeklyRanking> entities = IntStream.rangeClosed(1, top100.size())
                .mapToObj(i -> {
                    RankedProduct rp = top100.get(i - 1);
                    return WeeklyRanking.create(
                            i,
                            rp.productId(),
                            rp.score(),
                            startDate,
                            endDate);
                })
                .toList();

        weeklyRankingRepository.saveAll(entities);
    }
}
