package com.loopers.infrastructure.platform;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Component
public class MockDataPlatformSender implements DataPlatformSender {

    private final List<OrderResultMessage> sentMessages = new ArrayList<>();

    @Override
    public void sendOrderResult(OrderResultMessage message) {
        try {
            Thread.sleep(500); // 0.5초 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sentMessages.add(message);
        log.info("[MOCK] 데이터 플랫폼 전송: {}", message);
    }

    public void clear() {
        sentMessages.clear();
    }

}
