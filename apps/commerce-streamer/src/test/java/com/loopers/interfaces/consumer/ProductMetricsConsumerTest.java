package com.loopers.interfaces.consumer;

import com.loopers.application.metrics.ProductMetricsFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.metrics.ProductMetricsCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductMetricsConsumerTest {

    private final ProductMetricsFacade facade = mock(ProductMetricsFacade.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductMetricsConsumer consumer = new ProductMetricsConsumer(facade, objectMapper);

    @Test
    @DisplayName("like 토픽 → JSON 파싱 → Facade.processLikeMetrics 호출")
    void route_like() throws Exception {
        String payload = """
            {"eventId":"evt-1", "productId":100, "likeType":"like"}
        """;
        consumer.listen(List.of(payload), List.of("product-like-metrics"));

        ArgumentCaptor<ProductMetricsCommand> captor = ArgumentCaptor.forClass(ProductMetricsCommand.class); // productMetricsCommand 타입 인자 캡처할 수 있는 객체 생성
        verify(facade, times(1)).processLikeMetrics(captor.capture()); // facade의 processLikeMetrics 메서드가 1회 호출되었는지 검증, 호출 시 전달된 인자를 captor가 캡처
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
        verify(facade, times(1)).processStockMetrics(captor.capture());
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
        verify(facade, times(1)).processViewMetrics(captor.capture());
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

        verifyNoInteractions(facade); // facade가 호출되지 않았는지 검증
    }

    @Test
    @DisplayName("알 수 없는 토픽 → 아무 처리도 하지 않음")
    void unknown_topic() throws Exception {
        String payload = """
            {"eventId":"evt-5", "productId":104}
        """;

        consumer.listen(List.of(payload), List.of("unknown-topic"));

        verifyNoInteractions(facade);
    }

}
