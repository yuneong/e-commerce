package com.loopers.batch.writer;

import com.loopers.domain.ranking.MonthlyRanking;
import com.loopers.domain.ranking.MonthlyRankingRepository;
import com.loopers.dto.RankedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@StepScope
public class MonthlyRankingWriter implements ItemWriter<RankedProduct> {

    private final MonthlyRankingRepository monthlyRankingRepository;

    @Value("#{jobParameters['yearMonth']}")
    private String yearMonthStr;

    @Override
    public void write(Chunk<? extends RankedProduct> items) {
        if (items.isEmpty()) return;

        YearMonth yearMonth = YearMonth.parse(yearMonthStr);

        // productId 별 스코어 계산
        Map<Long, Double> scoreByProductId = items.getItems().stream()
                .collect(Collectors.groupingBy(
                        RankedProduct::productId,
                        Collectors.summingDouble(RankedProduct::score)
                ));

        // RankedProduct 리스트 변환, 100개 뽑아서 정렬
        List<RankedProduct> top100 = scoreByProductId.entrySet().stream()
                .map(e -> new RankedProduct(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(RankedProduct::score).reversed())
                .limit(100)
                .toList();;

        // 엔티티로 변환
        List<MonthlyRanking> entities = IntStream.rangeClosed(1, top100.size())
                .mapToObj(i -> {
                    RankedProduct rp = top100.get(i - 1);
                    return MonthlyRanking.create(
                            i,
                            rp.productId(),
                            rp.score(),
                            yearMonth);
                })
                .toList();

        monthlyRankingRepository.saveAll(entities);
    }

}
