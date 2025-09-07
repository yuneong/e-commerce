package com.loopers.interfaces.listener;

import com.loopers.domain.common.UserActionEnvelope;
import com.loopers.infrastructure.kafka.producer.UserActionEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionEventListener {

    private final UserActionEventProducer userActionEventProducer;

    @EventListener
    public void handle(UserActionEnvelope<?> event) {
        // 사용자 행위 로깅
//            String payloadJson = objectMapper.writeValueAsString(event.payload());
//            log.info("actionTypetion={} payload={} at={}", event.actionType(), payloadJson, event.occurredAt());

        // 카프카 사용자 행위 로깅
        userActionEventProducer.sendUserActionEvent(event);
    }

}
