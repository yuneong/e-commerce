package com.loopers.batch.reader;

import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.domain.ranking.WeeklyRankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MonthlyRankingReaderTest {

    private WeeklyRankingRepository weeklyRankingRepository;
    private MonthlyRankingReader reader;

    @BeforeEach
    void setUp() {
        weeklyRankingRepository = Mockito.mock(WeeklyRankingRepository.class);
        reader = new MonthlyRankingReader(weeklyRankingRepository);

        ReflectionTestUtils.setField(reader, "yearMonthStr", "2025-09");
    }

    @Test
    @DisplayName("성공: yearMonth 범위의 WeeklyRanking 데이터를 순차적으로 읽는다")
    void read_success() {
        // given
        WeeklyRanking r1 = WeeklyRanking.create(1, 101L, 90.5,
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 7));
        WeeklyRanking r2 = WeeklyRanking.create(2, 102L, 85.0,
                LocalDate.of(2025, 9, 8), LocalDate.of(2025, 9, 14));

        when(weeklyRankingRepository.findByWeekRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(r1, r2));

        // when & then
        assertThat(reader.read()).isEqualTo(r1);
        assertThat(reader.read()).isEqualTo(r2);
        assertThat(reader.read()).isNull(); // 데이터 끝
        verify(weeklyRankingRepository, times(1)).findByWeekRange(any(), any());
    }

    @Test
    @DisplayName("성공: 조회된 데이터가 없으면 read()는 null을 반환한다")
    void read_emptyList() {
        // given
        when(weeklyRankingRepository.findByWeekRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        // when
        WeeklyRanking result = reader.read();

        // then
        assertThat(result).isNull();
        verify(weeklyRankingRepository, times(1)).findByWeekRange(any(), any());
    }

    @Test
    @DisplayName("실패: yearMonth 파라미터가 잘못된 형식이면 DateTimeParseException 발생")
    void read_invalidYearMonth() {
        // given
        ReflectionTestUtils.setField(reader, "yearMonthStr", "2025/09");

        // when & then
        assertThatThrownBy(() -> reader.read())
                .isInstanceOf(java.time.format.DateTimeParseException.class);
        verifyNoInteractions(weeklyRankingRepository);
    }

    @Test
    @DisplayName("실패: Repository 호출 중 RuntimeException 발생 시 그대로 전파")
    void read_repositoryThrowsException() {
        // given
        when(weeklyRankingRepository.findByWeekRange(any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new RuntimeException("DB error"));

        // when & then
        assertThatThrownBy(() -> reader.read())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

}
