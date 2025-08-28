package com.loopers.interfaces.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.common.UserActionEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionEventListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @EventListener
    public void handle(UserActionEnvelope<?> event) {
        try {
            String payloadJson = objectMapper.writeValueAsString(event.payload());

            log.info("action={} payload={} at={}", event.actionType(), payloadJson, event.occurredAt());
        } catch (Exception e) {
            log.error("Failed to log user action: {}", event, e);
        }
    }

}
