package com.insurance.adminreportservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology, retry, and message converter for SmartSure events.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "smartsure.exchange";
    public static final String DLX = "smartsure.dlx";

    public static final String POLICY_QUEUE = "policy.queue";
    public static final String CLAIM_QUEUE = "claim.queue";
    public static final String CLAIM_REVIEW_QUEUE = "claim.review.queue";
    public static final String DLQ = "smartsure.dlq";

    public static final String POLICY_ROUTING_KEY = "policy.created";
    public static final String CLAIM_ROUTING_KEY = "claim.submitted";
    public static final String CLAIM_REVIEW_ROUTING_KEY = "claim.reviewed";
    public static final String DLQ_ROUTING_KEY = "smartsure.dlq";

    @Bean
    public TopicExchange smartSureExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue claimQueue() {
        return QueueBuilder.durable(CLAIM_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue claimReviewQueue() {
        return QueueBuilder.durable(CLAIM_REVIEW_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding claimBinding(Queue claimQueue, TopicExchange smartSureExchange) {
        return BindingBuilder.bind(claimQueue)
                .to(smartSureExchange)
                .with(CLAIM_ROUTING_KEY);
    }

    @Bean
    public Binding claimReviewBinding(Queue claimReviewQueue, TopicExchange smartSureExchange) {
        return BindingBuilder.bind(claimReviewQueue)
                .to(smartSureExchange)
                .with(CLAIM_REVIEW_ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 5000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
