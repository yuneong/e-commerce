package com.loopers.domain.ranking;

import com.loopers.support.generator.RedisKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RankingRepository rankingRepository;

    public List<Ranking> getDailyRankings(LocalDate date, Pageable page) {
        // key
        String key = RedisKeyGenerator.buildKey("ranking:all:", date);

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, page.getOffset(), page.getPageSize());

        return tuples.stream()
                .map(t -> Ranking.create(
                        Long.valueOf(t.getValue()),
                        t.getScore()
                ))
                .toList();
    }

    public List<Ranking> getWeeklyRankings(LocalDate today, Pageable page) {
        LocalDate lastWeekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastWeekEnd = today.minusWeeks(1).with(DayOfWeek.SUNDAY);

        List<WeeklyRanking> weeklyRankings = rankingRepository.findWeeklyRankingByDate(
                lastWeekStart,
                lastWeekEnd,
                page
        );

        return weeklyRankings.stream()
                .map(w -> Ranking.create(
                        w.getProductId(),
                        w.getScore()
                ))
                .toList();
    }

    public List<Ranking> getMonthlyRankings(LocalDate today, Pageable page) {
        YearMonth lastMonth = YearMonth.from(today).minusMonths(1);

        List<MonthlyRanking> monthlyRankings = rankingRepository.findMonthlyRankingByDate(
                lastMonth,
                page
        );

        return monthlyRankings.stream()
                .map(w -> Ranking.create(
                        w.getProductId(),
                        w.getScore()
                ))
                .toList();
    }

}
