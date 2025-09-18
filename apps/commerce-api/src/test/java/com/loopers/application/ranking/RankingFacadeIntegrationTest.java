package com.loopers.application.ranking;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.ranking.MonthlyRanking;
import com.loopers.domain.ranking.Ranking;
import com.loopers.domain.ranking.RankingRepository;
import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.support.generator.RedisKeyGenerator;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
class RankingFacadeIntegrationTest {

    @Autowired private RankingFacade rankingFacade;
    @Autowired private RedisTemplate<String, String> redisTemplate;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private RankingRepository rankingRepository;
    @Autowired private DatabaseCleanUp databaseCleanUp;
    @Autowired private RedisCleanUp redisCleanUp;

    private final Pageable pageable = PageRequest.of(0, 3);
    private List<Product> saveProducts;

    @BeforeEach
    void setup() {
        Brand brand = Brand.create("브랜드명", "브랜드설명", "http://brand-logo.jpg");
        Brand savedBrand = brandRepository.save(brand);

        List<Product> products = List.of(
                Product.create(savedBrand, "상품1", "상품설명1", "http://product1.jpg", 10000, 3),
                Product.create(savedBrand, "상품2", "상품설명2", "http://product2.jpg", 50000, 6),
                Product.create(savedBrand, "상품3", "상품설명3", "http://product3.jpg", 70000, 9)
        );
        saveProducts = productRepository.saveAll(products);
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    @Nested
    @DisplayName("daily 랭킹 조회")
    class DailyRanking {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("랭킹과 상품이 정상적으로 매핑되어 RankingInfo 리스트를 반환한다")
            void getRankings_success() {
                // given
                String key = RedisKeyGenerator.todayKey("ranking:all:");
                String today = LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.BASIC_ISO_DATE);

                List<Ranking> rankings = List.of(
                        Ranking.create(saveProducts.get(0).getId(), 10.5),
                        Ranking.create(saveProducts.get(1).getId(), 7.2),
                        Ranking.create(saveProducts.get(2).getId(), 3.1)
                );

                Set<ZSetOperations.TypedTuple<String>> tuples = rankings.stream()
                        .map(r -> new DefaultTypedTuple<>(r.getProductId().toString(), r.getScore()))
                        .collect(Collectors.toSet());
                redisTemplate.opsForZSet().add(key, tuples);

                // when
                List<RankingInfo> result = rankingFacade.getRankings(today, "DAILY", pageable);

                // then
                assertThat(result).hasSize(3);
                assertThat(result.get(0).productName()).isEqualTo("상품1");
                assertThat(result.get(0).rank()).isEqualTo(1);
                assertThat(result.get(1).rank()).isEqualTo(2);
                assertThat(result.get(2).rank()).isEqualTo(3);
            }
        }

        @Nested
        @DisplayName("예외 케이스")
        class ExceptionCase {

            @Test
            @DisplayName("Ranking 개수보다 Pageable 사이즈가 큰 경우 IndexOutOfBoundsException 발생")
            void getRankings_pageSizeTooLarge() {
                // given
                String today = LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.BASIC_ISO_DATE);

                List<Ranking> rankings = List.of(
                        Ranking.create(saveProducts.get(0).getId(), 10.5) // 1개만 저장
                );
                Set<ZSetOperations.TypedTuple<String>> tuples = rankings.stream()
                        .map(r -> new DefaultTypedTuple<>(r.getProductId().toString(), r.getScore()))
                        .collect(Collectors.toSet());
                redisTemplate.opsForZSet().add(today, tuples);


                Pageable oversizedPageable = PageRequest.of(0, 3); // 요청은 3개

                // when & then
                assertThatThrownBy(() -> rankingFacade.getRankings(today, "DAILY", oversizedPageable))
                        .isInstanceOf(IndexOutOfBoundsException.class);
            }

            @Test
            @DisplayName("Redis에 랭킹 데이터가 없을 경우 ArrayIndexOutOfBoundsException 발생")
            void getRankings_noDataInRedis() {
                // given
                String today = LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.BASIC_ISO_DATE);

                // Redis에서 해당 key 제거 (데이터 없는 상태 보장)
                redisTemplate.delete(today);

                // when & then
                assertThatThrownBy(() -> rankingFacade.getRankings(today, "DAILY", pageable))
                        .isInstanceOf(ArrayIndexOutOfBoundsException.class);
            }
        }
    }

    @Nested
    @DisplayName("주간 랭킹")
    class WeeklyRankingCase {

        @Test
        @DisplayName("주간 랭킹이 정상적으로 매핑되어 RankingInfo 리스트를 반환한다")
        void getWeeklyRankings_success() {
            // given
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            LocalDate lastWeekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY);
            LocalDate lastWeekEnd = today.minusWeeks(1).with(DayOfWeek.SUNDAY);

            WeeklyRanking wr1 = WeeklyRanking.create(1, saveProducts.get(0).getId(), 20.0, lastWeekStart, lastWeekEnd);
            WeeklyRanking wr2 = WeeklyRanking.create(2, saveProducts.get(1).getId(), 15.0, lastWeekStart, lastWeekEnd);
            WeeklyRanking wr3 = WeeklyRanking.create(3, saveProducts.get(2).getId(), 5.0, lastWeekStart, lastWeekEnd);

            rankingRepository.saveAllWeekly(List.of(wr1, wr2, wr3));

            String todayStr = today.format(DateTimeFormatter.BASIC_ISO_DATE);

            // when
            List<RankingInfo> result = rankingFacade.getRankings(todayStr, "WEEKLY", pageable);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).rank()).isEqualTo(1);
            assertThat(result.get(1).rank()).isEqualTo(2);
            assertThat(result.get(2).rank()).isEqualTo(3);
        }

        @Test
        @DisplayName("주간 랭킹 데이터가 없으면 ArrayIndexOutOfBoundsException 발생")
        void getWeeklyRankings_noData() {
            // given
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            String todayStr = today.format(DateTimeFormatter.BASIC_ISO_DATE);

            // when & then
            assertThatThrownBy(() -> rankingFacade.getRankings(todayStr, "WEEKLY", pageable))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("월간 랭킹")
    class MonthlyRankingCase {

        @Test
        @DisplayName("월간 랭킹이 정상적으로 매핑되어 RankingInfo 리스트를 반환한다")
        void getMonthlyRankings_success() {
            // given
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            YearMonth lastMonth = YearMonth.from(today).minusMonths(1);

            MonthlyRanking mr1 = MonthlyRanking.create(1, saveProducts.get(0).getId(), 50.0, lastMonth);
            MonthlyRanking mr2 = MonthlyRanking.create(2, saveProducts.get(1).getId(), 30.0, lastMonth);
            MonthlyRanking mr3 = MonthlyRanking.create(3, saveProducts.get(2).getId(), 10.0, lastMonth);

            rankingRepository.saveAllMonthly(List.of(mr1, mr2, mr3));

            String todayStr = today.format(DateTimeFormatter.BASIC_ISO_DATE);

            // when
            List<RankingInfo> result = rankingFacade.getRankings(todayStr, "MONTHLY", pageable);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).rank()).isEqualTo(1);
            assertThat(result.get(1).rank()).isEqualTo(2);
            assertThat(result.get(2).rank()).isEqualTo(3);
        }

        @Test
        @DisplayName("월간 랭킹 데이터가 없으면 ArrayIndexOutOfBoundsException 발생")
        void getMonthlyRankings_noData() {
            // given
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            String todayStr = today.format(DateTimeFormatter.BASIC_ISO_DATE);

            // when & then
            assertThatThrownBy(() -> rankingFacade.getRankings(todayStr, "MONTHLY", pageable))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }
    }

}
