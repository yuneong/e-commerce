package com.loopers.domain.ranking;

import com.loopers.application.metrics.MetricsCounter;
import com.loopers.support.generator.RedisKeyGenerator;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RankingServiceIntegrationTest {

    @Autowired private RankingService rankingService;
    @Autowired private RedisTemplate<String, String> redisTemplate;
    @Autowired private RedisCleanUp redisCleanUp;

    @AfterEach
    void cleanDatabase() {
        redisCleanUp.truncateAll();
    }

    @Test
    @DisplayName("성공 케이스: processRanking 호출 시 점수가 Redis ZSET에 저장되고 TTL이 설정된다")
    void processRanking_success() {
        // given
        MetricsCounter counter = new MetricsCounter();
        counter.setLikeCount(2);
        counter.setStockCount(3);
        counter.setViewCount(5);

        Map<Long, MetricsCounter> metricsMap = Map.of(101L, counter);

        // when
        rankingService.processRanking(metricsMap);

        // then
        String key = RedisKeyGenerator.todayKey("ranking:all:");
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        assertThat(tuples).isNotNull();
        ZSetOperations.TypedTuple<String> tuple = tuples.iterator().next();
        assertThat(tuple.getValue()).isEqualTo("101");
        assertThat(tuple.getScore()).isGreaterThan(0.0);

        // TTL 확인
        Long ttl = redisTemplate.getExpire(key);
        assertThat(ttl).isGreaterThan(0);
        assertThat(ttl).isLessThanOrEqualTo(Duration.ofDays(2).toSeconds());
    }

    @Test
    @DisplayName("성공 케이스: processRanking 호출 시 여러 상품 점수가 Redis ZSET에 저장되고 TTL이 설정된다")
    void processRanking_success_multipleProducts() {
        // given
        MetricsCounter counter1 = new MetricsCounter();
        counter1.setLikeCount(2);   // 점수 낮음 -> 랭킹 3위 예상
        counter1.setStockCount(3);
        counter1.setViewCount(5);

        MetricsCounter counter2 = new MetricsCounter();
        counter2.setLikeCount(10);  // 점수 높음 -> 랭킹 1위 예상
        counter2.setStockCount(10);
        counter2.setViewCount(1);

        MetricsCounter counter3 = new MetricsCounter();
        counter3.setLikeCount(5);   // 중간 점수 -> 랭킹 2위 예상
        counter3.setStockCount(3);
        counter3.setViewCount(2);

        Map<Long, MetricsCounter> metricsMap = Map.of(
                101L, counter1,
                102L, counter2,
                103L, counter3
        );

        // when
        rankingService.processRanking(metricsMap);

        // then
        String key = RedisKeyGenerator.todayKey("ranking:all:");
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);

        System.out.printf("Key: %s, Tuples: %s%n", key, tuples);

        assertThat(tuples).isNotNull();
        assertThat(tuples).hasSize(3);

        // 상품별 랭킹 확인
        Long rank101 = redisTemplate.opsForZSet().reverseRank(key, "101"); // 점수 높은 순
        Long rank102 = redisTemplate.opsForZSet().reverseRank(key, "102");
        Long rank103 = redisTemplate.opsForZSet().reverseRank(key, "103");

        System.out.printf("101 → %d위, 102 → %d위, 103 → %d위%n", rank101 + 1, rank102 + 1, rank103 + 1);

        assertThat(rank102).isEqualTo(0); // 1등
        assertThat(rank103).isEqualTo(1); // 2등
        assertThat(rank101).isEqualTo(2); // 3등

        // TTL 확인
        Long ttl = redisTemplate.getExpire(key);
        assertThat(ttl).isGreaterThan(0);
        assertThat(ttl).isLessThanOrEqualTo(Duration.ofDays(2).toSeconds());
    }

    @Test
    @DisplayName("예외 케이스: metricsCounters가 비어있으면 Redis에 아무것도 저장되지 않는다")
    void processRanking_emptyMetrics() {
        // given
        Map<Long, MetricsCounter> emptyMap = Map.of();

        // when
        rankingService.processRanking(emptyMap);

        // then
        String key = RedisKeyGenerator.todayKey("ranking:all:");
        Set<String> values = redisTemplate.opsForZSet().range(key, 0, -1);

        System.out.println("Values: " + values);

        assertThat(values).isEmpty();
    }

}
