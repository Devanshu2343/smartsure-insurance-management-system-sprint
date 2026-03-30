package com.insurance.claimsservice.config;

import feign.RequestInterceptor;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards gateway-injected security headers to downstream services.
 */
@Configuration
public class FeignHeaderConfig {

    @Bean
    public RequestInterceptor headerForwardingInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String email = request.getHeader("X-User-Email");
            String role = request.getHeader("X-User-Role");
            if (email != null && !email.isBlank()) {
                template.header("X-User-Email", email);
            }
            if (role != null && !role.isBlank()) {
                template.header("X-User-Role", role);
            }
        };
    }

    @Bean
    public RequestInterceptor tracingRequestInterceptor(Tracer tracer, Propagator propagator) {
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
