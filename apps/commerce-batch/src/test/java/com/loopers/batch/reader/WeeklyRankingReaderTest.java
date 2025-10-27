package com.loopers.batch.reader;

import com.loopers.domain.metrics.ProductMetricsRepository;
import com.loopers.dto.ProductMetricsSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeeklyRankingReaderTest {

    private ProductMetricsRepository productMetricsRepository;
    private WeeklyRankingReader reader;

    @BeforeEach
    void setUp() {
        productMetricsRepository = mock(ProductMetricsRepository.class);
        reader = new WeeklyRankingReader(productMetricsRepository);

        ReflectionTestUtils.setField(reader, "startDate", "2025-09-01");
        ReflectionTestUtils.setField(reader, "endDate", "2025-09-07");
    }

    @Nested
    @DisplayName("성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("정상적으로 데이터를 읽어온다")
        void read_success() {
            // given
            ProductMetricsSummary summary1 = new ProductMetricsSummary(1L, LocalDate.parse("2025-09-01"), 10L, 5L, 100L);
            ProductMetricsSummary summary2 = new ProductMetricsSummary(2L, LocalDate.parse("2025-09-07"), 20L, 8L, 200L);

            when(productMetricsRepository.findByIdProductIdAndDateBetween(
                    LocalDate.parse("2025-09-01"),
                    LocalDate.parse("2025-09-07"))
            ).thenReturn(List.of(summary1, summary2));

            // when & then
            assertThat(reader.read()).isEqualTo(summary1);
            assertThat(reader.read()).isEqualTo(summary2);
            assertThat(reader.read()).isNull(); // 다 읽고 나면 null
        }

        @Test
        @DisplayName("조회된 데이터가 없으면 바로 null 반환한다")
        void read_emptyList() {
            // given
            when(productMetricsRepository.findByIdProductIdAndDateBetween(
                    LocalDate.parse("2025-09-01"),
                    LocalDate.parse("2025-09-07"))
            ).thenReturn(List.of());

            // when & then
            assertThat(reader.read()).isNull();
        }
    }

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("Repository 호출 시 예외 발생하면 그대로 던진다")
        void read_repositoryException() {
            // given
            when(productMetricsRepository.findByIdProductIdAndDateBetween(
                    LocalDate.parse("2025-09-01"),
                    LocalDate.parse("2025-09-07"))
            ).thenThrow(new RuntimeException("DB 에러"));

            // when & then
            assertThatThrownBy(() -> reader.read())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB 에러");
        }

        @Test
        @DisplayName("startDate가 잘못된 형식이면 예외 발생")
        void read_invalidStartDate() {
            // given
            ReflectionTestUtils.setField(reader, "startDate", "invalid-date");

            // when & then
            assertThatThrownBy(() -> reader.read())
                    .isInstanceOf(java.time.format.DateTimeParseException.class);
        }
    }

}
