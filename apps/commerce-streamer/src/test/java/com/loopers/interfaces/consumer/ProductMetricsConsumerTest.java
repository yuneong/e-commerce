package com.loopers.interfaces.consumer;

import com.loopers.application.metrics.MetricsCounter;
import com.loopers.application.metrics.ProductMetricsFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.metrics.ProductMetricsCommand;
import com.loopers.application.ranking.RankingFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductMetricsConsumerTest {

    private final ProductMetricsFacade metricsFacade = mock(ProductMetricsFacade.class);
    private final RankingFacade rankingFacade = mock(RankingFacade.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductMetricsConsumer consumer = new ProductMetricsConsumer(metricsFacade, objectMapper, rankingFacade);

    @Test
    @DisplayName("like 토픽 → JSON 파싱 → Facade.processLikeMetrics 호출")
    void route_like() throws Exception {
        String payload = """
            {"eventId":"evt-1", "productId":100, "likeType":"like"}
        """;
        consumer.listen(List.of(payload), List.of("product-like-metrics"));

        ArgumentCaptor<ProductMetricsCommand> captor = ArgumentCaptor.forClass(ProductMetricsCommand.class); // productMetricsCommand 타입 인자 캡처할 수 있는 객체 생성
        verify(metricsFacade, times(1)).processLikeMetrics(captor.capture()); // facade의 processLikeMetrics 메서드가 1회 호출되었는지 검증, 호출 시 전달된 인자를 captor가 캡처
        ProductMetricsCommand cmd = captor.getValue(); // captor가 캡처한 인자 가져오기

        assertThat(cmd.eventId()).isEqualTo("evt-1");
        assertThat(cmd.productId()).isEqualTo(100L);
        assertThat(cmd.likeType()).isEqualTo("like");
    }

    @Test
    @DisplayName("stock 토픽 → JSON 파싱 → Facade.processStockMetrics 호출")
    void route_stock() throws Exception {
        String payload = """
            {"eventId":"evt-2", "productId":101, "stock":3, "changedType":"SUCCESS"}
        """;
        consumer.listen(List.of(payload), List.of("product-stock-metrics"));

        ArgumentCaptor<ProductMetricsCommand> captor = ArgumentCaptor.forClass(ProductMetricsCommand.class);
        verify(metricsFacade, times(1)).processStockMetrics(captor.capture());
        ProductMetricsCommand cmd = captor.getValue();

        assertThat(cmd.productId()).isEqualTo(101L);
        assertThat(cmd.stock()).isEqualTo(3);
        assertThat(cmd.changedType()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("view 토픽 → JSON 파싱 → Facade.processViewMetrics 호출")
    void route_view() throws Exception {
        String payload = """
            {"eventId":"evt-3", "productId":102}
        """;
        consumer.listen(List.of(payload), List.of("product-view-metrics"));

        ArgumentCaptor<ProductMetricsCommand> captor = ArgumentCaptor.forClass(ProductMetricsCommand.class);
        verify(metricsFacade, times(1)).processViewMetrics(captor.capture());
        ProductMetricsCommand cmd = captor.getValue();

        assertThat(cmd.productId()).isEqualTo(102L);
    }

    @Test
    @DisplayName("topic == null → 조용히 리턴 (호출 없음)")
    void null_topic() throws Exception {
        String payload = """
            {"eventId":"evt-4", "productId":103}
        """;

        consumer.listen(List.of(payload), null);

        verifyNoInteractions(metricsFacade); // facade가 호출되지 않았는지 검증
    }

    @Test
    @DisplayName("알 수 없는 토픽 → 아무 처리도 하지 않음")
    void unknown_topic() throws Exception {
        String payload = """
            {"eventId":"evt-5", "productId":104}
        """;

        consumer.listen(List.of(payload), List.of("unknown-topic"));

        verifyNoInteractions(metricsFacade);
    }

    @Test
    @DisplayName("여러 payload 처리 후 counters 값이 rankingFacade에 전달된다")
    void counters_are_aggregated_and_passed_to_rankingFacade() throws Exception {
        // given
        String likePayload = """
            {"eventId":"evt-10", "productId":201, "likeType":"like"}
        """;
        String stockPayload = """
            {"eventId":"evt-11", "productId":201, "stock":2, "changedType":"SUCCESS"}
        """;
        String viewPayload = """
            {"eventId":"evt-12", "productId":201}
        """;

        List<String> payloads = List.of(likePayload, stockPayload, viewPayload);
        List<String> topics = List.of("product-like-metrics", "product-stock-metrics", "product-view-metrics");

        // stub metricsFacade 리턴값 (processLikeMetrics 등에서 int 반환)
        when(metricsFacade.processLikeMetrics(any())).thenReturn(1);
        when(metricsFacade.processStockMetrics(any())).thenReturn(2);
        when(metricsFacade.processViewMetrics(any())).thenReturn(3);

        // when
        consumer.listen(payloads, topics);

        // then - rankingFacade 호출된 counters 확인
        ArgumentCaptor<Map<Long, MetricsCounter>> captor = ArgumentCaptor.forClass(Map.class);
        verify(rankingFacade, times(1)).processRanking(captor.capture());

        Map<Long, MetricsCounter> capturedCounters = captor.getValue();
        MetricsCounter counter = capturedCounters.get(201L);

        assertThat(counter.getLikeCount()).isEqualTo(1);
        assertThat(counter.getStockCount()).isEqualTo(2);
        assertThat(counter.getViewCount()).isEqualTo(3);
    }

}
