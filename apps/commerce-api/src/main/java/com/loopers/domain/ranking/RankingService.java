package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;

    public String buildKey(LocalDate date) {
        return "ranking:all:" + date.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    public List<Ranking> getRankings(LocalDate date, Pageable page) {
        // key
        String key = buildKey(date);

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, page.getOffset(), page.getPageSize());

        // 콜드 스타트
        if (tuples == null) {
            return List.of();
        }

        return tuples.stream()
                .map(t -> Ranking.create(
                        Long.valueOf(t.getValue()),
                        t.getScore()
                ))
                .toList();
    }

}
