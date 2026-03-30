package com.insurance.claimsservice.service;

import com.insurance.claimsservice.config.RabbitMQConfig;
import com.insurance.claimsservice.dto.event.ClaimReviewedEvent;
import com.insurance.claimsservice.dto.event.ClaimReviewedMessage;
import com.insurance.claimsservice.dto.event.ClaimSubmittedEvent;
import com.insurance.claimsservice.dto.event.ClaimSubmittedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes claim lifecycle events to RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishClaimSubmitted(ClaimSubmittedEvent event) {
        try {
            ClaimSubmittedMessage message = ClaimSubmittedMessage.from(event);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.CLAIM_ROUTING_KEY, message);
            log.info("Published claim.submitted event for claimId={} eventId={}",
                    event.claimId(), message.eventId());
        } catch (AmqpException exception) {
            log.error("Failed to publish claim.submitted event for claimId={}", event.claimId(), exception);
        }
    }

    public void publishClaimReviewed(ClaimReviewedEvent event) {
        try {
            ClaimReviewedMessage message = ClaimReviewedMessage.from(event);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.CLAIM_REVIEW_ROUTING_KEY, message);
            log.info("Published claim.reviewed event for claimId={} eventId={}",
                    event.claimId(), message.eventId());
        } catch (AmqpException exception) {
            log.error("Failed to publish claim.reviewed event for claimId={}", event.claimId(), exception);
        }
    }
}
