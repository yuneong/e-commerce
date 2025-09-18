package com.loopers.batch.writer;

import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.domain.ranking.WeeklyRankingRepository;
import com.loopers.dto.RankedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.item.Chunk;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WeeklyRankingWriterTest {

    private WeeklyRankingRepository weeklyRankingRepository;
    private WeeklyRankingWriter writer;

    @BeforeEach
    void setUp() {
        weeklyRankingRepository = mock(WeeklyRankingRepository.class);
        writer = new WeeklyRankingWriter(weeklyRankingRepository);

        ReflectionTestUtils.setField(writer, "startDate", "2025-09-01");
        ReflectionTestUtils.setField(writer, "endDate", "2025-09-07");
    }

    @Nested
    @DisplayName("성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("빈 아이템이면 saveAll 호출하지 않는다")
        void emptyItems() throws Exception {
            // when
            writer.write(Chunk.of());

            // then
            verify(weeklyRankingRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("productId별 점수를 합산하고 정렬 후 저장한다")
        void aggregateAndSave() throws Exception {
            // given
            RankedProduct p1 = new RankedProduct(1L, 10.0);
            RankedProduct p2 = new RankedProduct(1L, 15.0); // 같은 productId -> 합산
            RankedProduct p3 = new RankedProduct(2L, 5.0);

            // when
            writer.write(Chunk.of(p1, p2, p3));

            // then
            ArgumentCaptor<List<WeeklyRanking>> captor = ArgumentCaptor.forClass(List.class);
            verify(weeklyRankingRepository, times(1)).saveAll(captor.capture());

            List<WeeklyRanking> saved = captor.getValue();

            assertThat(saved).hasSize(2); // productId 1, 2 -> 2개만 저장

            assertThat(saved.get(0).getRank()).isEqualTo(1); // score 25.0 -> 1위
            assertThat(saved.get(0).getProductId()).isEqualTo(1L);

            assertThat(saved.get(1).getRank()).isEqualTo(2);
            assertThat(saved.get(1).getProductId()).isEqualTo(2L);

            assertThat(saved.get(0).getWeekStart()).isEqualTo(LocalDate.parse("2025-09-01"));
            assertThat(saved.get(0).getWeekEnd()).isEqualTo(LocalDate.parse("2025-09-07"));
        }

        @Test
        @DisplayName("100개 초과면 상위 100개만 저장한다")
        void limitTop100() throws Exception {
            // given
            List<RankedProduct> items =
                    // productId 1~150, score = i
                    IntStream.rangeClosed(1, 150)
                            .mapToObj(i -> new RankedProduct((long) i, (double) i))
                            .toList();
            // when
            writer.write(Chunk.of(items.toArray(new RankedProduct[0])));

            // then
            ArgumentCaptor<List<WeeklyRanking>> captor = ArgumentCaptor.forClass(List.class);
            verify(weeklyRankingRepository).saveAll(captor.capture());

            List<WeeklyRanking> saved = captor.getValue();

            assertThat(saved).hasSize(100); // 상위 100개만 저장
            assertThat(saved.get(0).getProductId()).isEqualTo(150L); // 가장 높은 점수
            assertThat(saved.get(99).getProductId()).isEqualTo(51L); // 51 ~ 150
        }
    }

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("저장 과정에서 예외 발생 시 그대로 던진다")
        void repositoryException() {
            // given
            RankedProduct p = new RankedProduct(1L, 10.0);
            doThrow(new RuntimeException("DB Error"))
                    .when(weeklyRankingRepository).saveAll(any());

            // when & then
            assertThatThrownBy(() -> writer.write(Chunk.of(p)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB Error");
        }

        @Test
        @DisplayName("startDate가 잘못된 값이면 DateTimeParseException 발생")
        void invalidStartDate() {
            // given
            ReflectionTestUtils.setField(writer, "startDate", "invalid-date");
            RankedProduct p = new RankedProduct(1L, 10.0);

            // when & then
            assertThatThrownBy(() -> writer.write(Chunk.of(p)))
                    .isInstanceOf(java.time.format.DateTimeParseException.class);
        }
    }
}
