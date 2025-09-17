package com.loopers.batch.writer;

import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.domain.ranking.WeeklyRankingRepository;
import com.loopers.dto.RankedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class WeeklyRankingWriter implements ItemWriter<RankedProduct> {

    private final WeeklyRankingRepository weeklyRankingRepository;

    @Value("#{jobParameters['startDate']}")
    private String startDate;

    @Value("#{jobParameters['endDate']}")
    private String endDate;

    @Override
    public void write(Chunk<? extends RankedProduct> items) {
        if (items.isEmpty()) return;

        LocalDate parsedStartDate = LocalDate.parse(startDate);
        LocalDate parsedEndDate = LocalDate.parse(endDate);

        // productId 별 스코어 계산
        Map<Long, Double> scoreByProductId = items.getItems().stream()
                .collect(Collectors.groupingBy(
                        RankedProduct::productId,
                        Collectors.summingDouble(RankedProduct::score)
                ));

        // RankedProduct 리스트 변환
        List<RankedProduct> rankedProducts = scoreByProductId.entrySet().stream()
                .map(e -> new RankedProduct(e.getKey(), e.getValue()))
                .toList();

        // 100개 뽑아서 정렬
        List<RankedProduct> top100 = rankedProducts.stream()
                .sorted(Comparator.comparingDouble(RankedProduct::score).reversed())
                .limit(100)
                .toList();

        // 엔티티로 변환
        List<WeeklyRanking> entities = IntStream.rangeClosed(1, top100.size())
                .mapToObj(i -> {
                    RankedProduct rp = top100.get(i - 1);
                    return WeeklyRanking.create(
                            i,
                            rp.productId(),
                            rp.score(),
                            parsedStartDate,
                            parsedEndDate);
                })
                .toList();

        weeklyRankingRepository.saveAll(entities);
    }

}
