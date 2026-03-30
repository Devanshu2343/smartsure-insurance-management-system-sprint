package com.insurance.adminreportservice.listener;

import com.insurance.adminreportservice.config.RabbitMQConfig;
import com.insurance.adminreportservice.dto.event.ClaimReviewedMessage;
import com.insurance.adminreportservice.dto.event.ClaimSubmittedMessage;
import com.insurance.adminreportservice.service.AuditEventService;
import com.insurance.adminreportservice.service.IdempotencyService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Consumes claim events for auditing and reporting.
 */
@Slf4j
@Service
public class ClaimEventListener {

    private final IdempotencyService idempotencyService;
    private final AuditEventService auditEventService;
    private final int submittedVersion;
    private final int reviewedVersion;
    private final Counter submittedDuplicateCounter;
    private final Counter reviewedDuplicateCounter;
    private final Timer submittedProcessingTimer;
    private final Timer reviewedProcessingTimer;

    public ClaimEventListener(IdempotencyService idempotencyService,
                              AuditEventService auditEventService,
                              MeterRegistry meterRegistry,
                              @Value("${events.claim.submitted.version:1}") int submittedVersion,
                              @Value("${events.claim.reviewed.version:1}") int reviewedVersion) {
        this.idempotencyService = idempotencyService;
        this.auditEventService = auditEventService;
        this.submittedVersion = submittedVersion;
        this.reviewedVersion = reviewedVersion;
        this.submittedDuplicateCounter = Counter.builder("events.duplicates")
                .description("Duplicate events ignored by consumers")
                .tag("service", "admin-report-service")
                .tag("eventType", "claim.submitted")
                .register(meterRegistry);
        this.reviewedDuplicateCounter = Counter.builder("events.duplicates")
                .description("Duplicate events ignored by consumers")
                .tag("service", "admin-report-service")
                .tag("eventType", "claim.reviewed")
                .register(meterRegistry);
        this.submittedProcessingTimer = Timer.builder("events.processing")
                .description("Event processing latency")
                .tag("service", "admin-report-service")
                .tag("eventType", "claim.submitted")
                .register(meterRegistry);
        this.reviewedProcessingTimer = Timer.builder("events.processing")
                .description("Event processing latency")
                .tag("service", "admin-report-service")
                .tag("eventType", "claim.reviewed")
                .register(meterRegistry);
    }

    @RabbitListener(queues = RabbitMQConfig.CLAIM_QUEUE)
    public void handleClaimSubmitted(ClaimSubmittedMessage message) {
        submittedProcessingTimer.record(() -> {
            if (message == null || message.payload() == null) {
                log.warn("Received empty claim.submitted message");
                return;
            }
            if (message.schemaVersion() != submittedVersion) {
                log.warn("Unsupported claim.submitted schemaVersion={}", message.schemaVersion());
                return;
            }
            if (idempotencyService.isDuplicate(message.idempotencyKey())) {
                submittedDuplicateCounter.increment();
                log.info("Duplicate claim.submitted message ignored. eventId={}", message.eventId());
                return;
            }

            auditEventService.record(message.eventType(), message);
            log.info("Received claim.submitted event. claimId={}, status={}, eventId={}",
                    message.payload().claimId(), message.payload().status(), message.eventId());
        });
    }

    @RabbitListener(queues = RabbitMQConfig.CLAIM_REVIEW_QUEUE)
    public void handleClaimReviewed(ClaimReviewedMessage message) {
        reviewedProcessingTimer.record(() -> {
            if (message == null || message.payload() == null) {
                log.warn("Received empty claim.reviewed message");
                return;
            }
            if (message.schemaVersion() != reviewedVersion) {
                log.warn("Unsupported claim.reviewed schemaVersion={}", message.schemaVersion());
                return;
            }
            if (idempotencyService.isDuplicate(message.idempotencyKey())) {
                reviewedDuplicateCounter.increment();
                log.info("Duplicate claim.reviewed message ignored. eventId={}", message.eventId());
                return;
            }

            auditEventService.record(message.eventType(), message);
            log.info("Received claim.reviewed event. claimId={}, decision={}, eventId={}",
                    message.payload().claimId(), message.payload().decision(), message.eventId());
        });
    }
}
