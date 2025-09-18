package com.loopers.batch.processor;

import com.loopers.dto.ProductMetricsSummary;
import com.loopers.dto.RankedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyRankingProcessorTest {

    private WeeklyRankingProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new WeeklyRankingProcessor();

        ReflectionTestUtils.setField(processor, "likeWeight", 0.2d);
        ReflectionTestUtils.setField(processor, "orderWeight", 0.7d);
        ReflectionTestUtils.setField(processor, "viewWeight", 0.1d);
    }

    @Nested
    @DisplayName("성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("오늘 데이터는 decay 1.0 적용된다")
        void process_todayData() throws Exception {
            ProductMetricsSummary summary =
                    new ProductMetricsSummary(1L, LocalDate.now(), 10L, 5L, 100L);

            RankedProduct ranked = processor.process(summary);

            double expectedBase = (0.2 * 10) + (0.7 * 5) + (0.1 * 100); // 2 + 3.5 + 10 = 15.5
            double expectedFinal = expectedBase * 1.0;

            assertThat(ranked.productId()).isEqualTo(1L);
            assertThat(ranked.score()).isEqualTo(expectedFinal);
        }

        @Test
        @DisplayName("3일 지난 데이터는 decay 0.4 적용된다")
        void process_3daysAgoData() throws Exception {
            ProductMetricsSummary summary =
                    new ProductMetricsSummary(2L, LocalDate.now().minusDays(3), 2L, 3L, 10L);

            RankedProduct ranked = processor.process(summary);

            double expectedBase = (0.2 * 2) + (0.7 * 3) + (0.1 * 10); // 0.4 + 2.1 + 1 = 3.5
            double expectedFinal = expectedBase * 0.4;

            assertThat(ranked.score()).isEqualTo(expectedFinal);
        }

        @Test
        @DisplayName("7일 초과 데이터는 score 0.0 처리된다")
        void process_olderThan7days() throws Exception {
            ProductMetricsSummary summary =
                    new ProductMetricsSummary(3L, LocalDate.now().minusDays(10), 5L, 5L, 5L);

            RankedProduct ranked = processor.process(summary);

            assertThat(ranked.score()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("summary 가 null 이면 NPE 발생")
        void process_nullSummary() {
            assertThatThrownBy(() -> processor.process(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("summary.date 가 null 이면 예외 발생")
        void process_nullDate() {
            ProductMetricsSummary summary =
                    new ProductMetricsSummary(4L, null, 1L, 1L, 1L);

            assertThatThrownBy(() -> processor.process(summary))
                    .isInstanceOf(NullPointerException.class);
        }
    }

}
