package com.insurance.policyservice.config;

import feign.RequestInterceptor;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Propagates trace context headers for downstream Feign calls.
 */
@Configuration
public class FeignTracingConfig {

    private final Tracer tracer;
    private final Propagator propagator;

    public FeignTracingConfig(Tracer tracer, Propagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    @Bean
    public RequestInterceptor tracingRequestInterceptor() {
        return template -> {
            Span span = tracer.currentSpan();
            if (span == null) {
                return;
            }
            propagator.inject(span.context(), template,
                    (carrier, key, value) -> carrier.header(key, value));
        };
    }
}
