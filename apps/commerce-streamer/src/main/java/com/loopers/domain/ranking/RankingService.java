package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RankingService {

    public final RankingRepository rankingRepository;
    public final RankingScorePolicy scorePolicy;
    public static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    public String buildKey(LocalDate date) {
        return "ranking:all:" + date.format(DAY);
    }

    public String todayKey() {
        return buildKey(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }

    public void recordLike(Long productId) {
        double score = scorePolicy.scoreFor(RankingEventType.LIKE, RankingScorePolicy.RankingContext.empty());
        rankingRepository.increaseScore(todayKey(), productId, score);
    }

    public void recordOrder(Long productId, int amount, int price) {
        double score = scorePolicy.scoreFor(RankingEventType.ORDER, RankingScorePolicy.RankingContext.order(price, amount));
        rankingRepository.increaseScore(todayKey(), productId, score);
    }

    public void recordView(Long productId) {
        double score = scorePolicy.scoreFor(RankingEventType.VIEW, RankingScorePolicy.RankingContext.empty());
        rankingRepository.increaseScore(todayKey(), productId, score);
    }

    public void setTodayRankingExpire() {
        rankingRepository.expire(buildKey(LocalDate.now()), 2 * 24 * 60 * 60); // 2일
    }

}
