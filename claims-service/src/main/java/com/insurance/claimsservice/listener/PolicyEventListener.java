package com.insurance.claimsservice.listener;

import com.insurance.claimsservice.config.RabbitMQConfig;
import com.insurance.claimsservice.dto.event.PolicyCreatedMessage;
import com.insurance.claimsservice.service.AuditEventService;
import com.insurance.claimsservice.service.IdempotencyService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Consumes policy creation events to prepare claim eligibility workflows.
 */
@Slf4j
@Service
public class PolicyEventListener {

    private final IdempotencyService idempotencyService;
    private final AuditEventService auditEventService;
    private final int supportedVersion;
    private final Counter duplicateCounter;
    private final Timer processingTimer;

    public PolicyEventListener(IdempotencyService idempotencyService,
                               AuditEventService auditEventService,
                               MeterRegistry meterRegistry,
                               @Value("${events.policy.created.version:1}") int supportedVersion) {
        this.idempotencyService = idempotencyService;
        this.auditEventService = auditEventService;
        this.supportedVersion = supportedVersion;
        this.duplicateCounter = Counter.builder("events.duplicates")
                .description("Duplicate events ignored by consumers")
                .tag("service", "claims-service")
                .tag("eventType", "policy.created")
                .register(meterRegistry);
        this.processingTimer = Timer.builder("events.processing")
                .description("Event processing latency")
                .tag("service", "claims-service")
                .tag("eventType", "policy.created")
                .register(meterRegistry);
    }

    @RabbitListener(queues = RabbitMQConfig.POLICY_QUEUE)
    public void handlePolicyCreated(PolicyCreatedMessage message) {
        processingTimer.record(() -> {
            if (message == null || message.payload() == null) {
                log.warn("Received empty policy.created message");
                return;
            }
            if (message.schemaVersion() != supportedVersion) {
                log.warn("Unsupported policy.created schemaVersion={}", message.schemaVersion());
                return;
            }
            if (idempotencyService.isDuplicate(message.idempotencyKey())) {
                duplicateCounter.increment();
                log.info("Duplicate policy.created message ignored. eventId={}", message.eventId());
                return;
            }

            auditEventService.record(message.eventType(), message);
            log.info("Received policy.created event. policyId={}, policyNumber={}, status={}, eventId={}",
                    message.payload().policyId(),
                    message.payload().policyNumber(),
                    message.payload().status(),
                    message.eventId());
        });
    }
}
