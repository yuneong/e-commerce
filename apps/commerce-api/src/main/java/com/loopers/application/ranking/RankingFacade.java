package com.loopers.application.ranking;


import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.Ranking;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@RequiredArgsConstructor
@Component
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductService productService;

    public List<RankingInfo> getRankings(String date, String period, Pageable page) {
        // 문자열 -> LocalDate 변환
        LocalDate localDate = (date != null)
                ? LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)
                : LocalDate.now(ZoneId.of("Asia/Seoul"));

        // period → enum
        RankingPeriod rankingPeriod = RankingPeriod.fromString(period);

        // 랭킹 조회
        List<Ranking> rankings = switch (rankingPeriod) {
            case DAILY -> rankingService.getDailyRankings(localDate, page);
            case WEEKLY -> rankingService.getWeeklyRankings(localDate, page);
            case MONTHLY -> rankingService.getMonthlyRankings(localDate, page);
        };

        // 상품 조회
        List<Long> productIds = rankings.stream()
                .map(Ranking::getProductId)
                .toList();

        List<Product> products = productService.getProductsByIdIn(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 매핑
        return IntStream.range(0, page.getPageSize())
                .mapToObj(i -> {
                    Ranking r = rankings.get(i);
                    return RankingInfo.from(
                            productMap.get(r.getProductId()),
                            r.getScore(),
                            i + 1
                    );
                })
                .toList();
    }

}
