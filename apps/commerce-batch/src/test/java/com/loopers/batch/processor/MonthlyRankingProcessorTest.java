package com.loopers.batch.processor;

import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.dto.RankedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthlyRankingProcessorTest {

    private MonthlyRankingProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MonthlyRankingProcessor();
        ReflectionTestUtils.setField(processor, "yearMonthStr", "2025-08");
    }

    @Test
    @DisplayName("성공: 주간이 월 안에 완전히 포함된 경우")
    void process_fullInsideMonth() throws Exception {
        // given
        WeeklyRanking weekly = WeeklyRanking.create(1, 101L, 70.0,
                LocalDate.of(2025, 8, 5),
                LocalDate.of(2025, 8, 11)
        );

        // when
        RankedProduct result = processor.process(weekly);

        // then
        assertThat(result).isNotNull();
        assertThat(result.productId()).isEqualTo(101L);
        assertThat(result.score()).isEqualTo(70.0); // 그대로 사용
    }

    @Test
    @DisplayName("성공: 주간 시작이 월 시작 이전이면 앞 부분은 잘려 계산된다")
    void process_trimStart() throws Exception {
        // given (7/28~8/3, 총 7일 / score=70 -> 하루 10점, 월에는 8/1~8/3 -> 3일만 반영)
        WeeklyRanking weekly = WeeklyRanking.create(1, 102L, 70.0,
                LocalDate.of(2025, 7, 28),
                LocalDate.of(2025, 8, 3)
        );

        // when
        RankedProduct result = processor.process(weekly);

        // then
        assertThat(result).isNotNull();
        assertThat(result.score()).isEqualTo(30.0); // 10점 * 3일
    }

    @Test
    @DisplayName("성공: 주간 끝이 월 끝 이후면 뒷 부분은 잘려 계산된다")
    void process_trimEnd() throws Exception {
        // given (8/28~9/3, 총 7일 / score=70 -> 하루 10점, 월에는 8/28~8/31 -> 4일만 반영)
        WeeklyRanking weekly = WeeklyRanking.create(1, 103L, 70.0,
                LocalDate.of(2025, 8, 28),
                LocalDate.of(2025, 9, 3)
        );

        // when
        RankedProduct result = processor.process(weekly);

        // then
        assertThat(result).isNotNull();
        assertThat(result.score()).isEqualTo(40.0); // 10점 * 4일
    }

    @Test
    @DisplayName("실패: 주간이 월과 전혀 겹치지 않으면 null 반환")
    void process_noOverlap() throws Exception {
        // given (7월 주간)
        WeeklyRanking weekly = WeeklyRanking.create(1, 105L, 100.0,
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 7)
        );

        // when
        RankedProduct result = processor.process(weekly);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("실패: yearMonthStr 포맷이 잘못되면 DateTimeParseException 발생")
    void process_invalidYearMonth() {
        // given
        ReflectionTestUtils.setField(processor, "yearMonthStr", "2025/08");
        WeeklyRanking weekly = WeeklyRanking.create(1, 106L, 50.0,
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 7)
        );

        // when & then
        assertThatThrownBy(() -> processor.process(weekly))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
    }

}
