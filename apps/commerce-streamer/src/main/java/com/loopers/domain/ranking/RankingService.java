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
        rankingRepository.increaseScore(todayKey(), productId, scorePolicy.scoreFor(RankingEventType.LIKE));
    }

    public void recordOrder(Long productId) {
        rankingRepository.increaseScore(todayKey(), productId, scorePolicy.scoreFor(RankingEventType.ORDER));
    }

    public void recordView(Long productId) {
        rankingRepository.increaseScore(todayKey(), productId, scorePolicy.scoreFor(RankingEventType.VIEW));
    }

    public void setTodayRankingExpire() {
        rankingRepository.expire(buildKey(LocalDate.now()), 2 * 24 * 60 * 60); // 2일
    }

}
