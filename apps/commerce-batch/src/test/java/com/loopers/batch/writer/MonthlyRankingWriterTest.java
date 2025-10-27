package com.loopers.batch.writer;

import com.loopers.domain.ranking.MonthlyRanking;
import com.loopers.domain.ranking.MonthlyRankingRepository;
import com.loopers.dto.RankedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.item.Chunk;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MonthlyRankingWriterTest {

    private MonthlyRankingRepository monthlyRankingRepository;
    private MonthlyRankingWriter writer;

    @BeforeEach
    void setUp() {
        monthlyRankingRepository = mock(MonthlyRankingRepository.class);
        writer = new MonthlyRankingWriter(monthlyRankingRepository);

        ReflectionTestUtils.setField(writer, "yearMonthStr", "2025-09");
    }

    @Test
    @DisplayName("성공: 데이터가 없으면 saveAll이 호출되지 않는다")
    void write_emptyItems() throws Exception {
        // when
        writer.write(Chunk.of());

        // then
        verifyNoInteractions(monthlyRankingRepository);
    }

    @Test
    @DisplayName("성공: 100개 이하 데이터는 모두 저장된다")
    void write_under100Items() throws Exception {
        // given
        List<RankedProduct> items = List.of(
                new RankedProduct(1L, 50.0),
                new RankedProduct(2L, 70.0),
                new RankedProduct(3L, 60.0)
        );

        // when
        writer.write(Chunk.of(items.toArray(new RankedProduct[0])));

        // then
        ArgumentCaptor<List<MonthlyRanking>> captor = ArgumentCaptor.forClass(List.class);
        verify(monthlyRankingRepository).saveAll(captor.capture());

        List<MonthlyRanking> saved = captor.getValue();
        assertThat(saved).hasSize(3);
        assertThat(saved.get(0).getProductId()).isEqualTo(2L);
        assertThat(saved.get(0).getRank()).isEqualTo(1);
        assertThat(saved.get(1).getRank()).isEqualTo(2);
        assertThat(saved.get(2).getRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("성공: 100개 초과 데이터는 상위 100개만 저장된다")
    void write_over100Items() throws Exception {
        // given (1~150 productId, score = i)
        List<RankedProduct> items =
                java.util.stream.IntStream.rangeClosed(1, 150)
                        .mapToObj(i -> new RankedProduct((long) i, (double) i))
                        .toList();

        // when
        writer.write(Chunk.of(items.toArray(new RankedProduct[0])));

        // then
        ArgumentCaptor<List<MonthlyRanking>> captor = ArgumentCaptor.forClass(List.class);
        verify(monthlyRankingRepository).saveAll(captor.capture());

        List<MonthlyRanking> saved = captor.getValue();
        assertThat(saved).hasSize(100);
        assertThat(saved.get(0).getProductId()).isEqualTo(150L); // 제일 높은 점수
        assertThat(saved.get(99).getProductId()).isEqualTo(51L); // 51 ~ 150 까지 저장
    }

    @Test
    @DisplayName("성공: 같은 productId는 점수가 합산된다")
    void write_duplicateProductId() throws Exception {
        // given
        List<RankedProduct> items = List.of(
                new RankedProduct(10L, 30.0),
                new RankedProduct(10L, 20.0),
                new RankedProduct(20L, 40.0)
        );

        // when
        writer.write(Chunk.of(items.toArray(new RankedProduct[0])));

        // then
        ArgumentCaptor<List<MonthlyRanking>> captor = ArgumentCaptor.forClass(List.class);
        verify(monthlyRankingRepository).saveAll(captor.capture());

        List<MonthlyRanking> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getProductId()).isEqualTo(10L); // 30+20=50점 -> rank 1
        assertThat(saved.get(1).getProductId()).isEqualTo(20L); // 40점 -> rank 2
    }

    @Test
    @DisplayName("실패: yearMonthStr이 잘못된 포맷이면 DateTimeParseException 발생")
    void write_invalidYearMonth() {
        // given
        ReflectionTestUtils.setField(writer, "yearMonthStr", "2025/09");
        List<RankedProduct> items = List.of(new RankedProduct(1L, 10.0));

        // when & then
        assertThatThrownBy(() -> writer.write(Chunk.of(items.toArray(new RankedProduct[0]))))
                .isInstanceOf(java.time.format.DateTimeParseException.class);

        verifyNoInteractions(monthlyRankingRepository);
    }

    @Test
    @DisplayName("실패: Repository에서 RuntimeException 발생 시 그대로 전파된다")
    void write_repositoryThrowsException() {
        // given
        List<RankedProduct> items = List.of(new RankedProduct(1L, 10.0));
        doThrow(new RuntimeException("DB error"))
                .when(monthlyRankingRepository).saveAll(any());

        // when & then
        assertThatThrownBy(() -> writer.write(Chunk.of(items.toArray(new RankedProduct[0]))))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

}
