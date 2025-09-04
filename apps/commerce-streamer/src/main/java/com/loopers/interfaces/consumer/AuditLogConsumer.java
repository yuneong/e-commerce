package com.loopers.interfaces.consumer;

import com.loopers.application.auditlog.AuditLogCommand;
import com.loopers.application.auditlog.AuditLogFacade;
import com.loopers.config.kafka.KafkaConfig;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogConsumer {

    private final AuditLogFacade auditLogFacade;

    @KafkaListener(
            topics = {"user-action-events"},
            groupId = "audit-log-group",
            containerFactory = KafkaConfig.STRING_BATCH_LISTENER
    )
    public void listen(List<ConsumerRecord<String, String>> records) {
        for (ConsumerRecord<String, String> record : records) {
            String payload = record.value();
            String userId = record.key();

            String eventType = "";
            if (record.headers().lastHeader("eventType") != null) {
                eventType = new String(record.headers().lastHeader("eventType").value(), StandardCharsets.UTF_8);
            }

            if (StringUtils.isBlank(userId)) {
                log.warn("userId is blank, payload = {}", payload);
                continue;
            }

            AuditLogCommand command = AuditLogCommand.of(payload, eventType);
            auditLogFacade.processAuditLog(command);
        }
    }
}
