package com.insurance.policyservice.service;

import com.insurance.policyservice.config.RabbitMQConfig;
import com.insurance.policyservice.dto.event.PolicyCreatedEvent;
import com.insurance.policyservice.dto.event.PolicyCreatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes policy lifecycle events to RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPolicyCreated(PolicyCreatedEvent event) {
        try {
            PolicyCreatedMessage message = PolicyCreatedMessage.from(event);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.POLICY_ROUTING_KEY, message);
            log.info("Published policy.created event for policyId={} eventId={}",
                    event.policyId(), message.eventId());
        } catch (AmqpException exception) {
            log.error("Failed to publish policy.created event for policyId={}", event.policyId(), exception);
        }
    }
}
