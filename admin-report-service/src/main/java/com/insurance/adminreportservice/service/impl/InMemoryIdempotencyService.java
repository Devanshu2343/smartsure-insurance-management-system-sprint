package com.insurance.adminreportservice.service.impl;

import com.insurance.adminreportservice.service.IdempotencyService;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * In-memory idempotency store for event keys.
 */
@Service
public class InMemoryIdempotencyService implements IdempotencyService {

    private static final int CLEANUP_THRESHOLD = 5000;

    private final Map<String, Long> processed = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public InMemoryIdempotencyService(@Value("${audit.idempotency.ttl-seconds:3600}") long ttlSeconds) {
        this.ttlMillis = Duration.ofSeconds(ttlSeconds).toMillis();
    }

    @Override
    public boolean isDuplicate(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }

        long now = System.currentTimeMillis();
        Long existing = processed.putIfAbsent(idempotencyKey, now);
        if (existing == null) {
            cleanupIfNeeded(now);
            return false;
        }

        if (now - existing > ttlMillis) {
            processed.put(idempotencyKey, now);
            cleanupIfNeeded(now);
            return false;
        }

        return true;
    }

    private void cleanupIfNeeded(long now) {
        if (processed.size() < CLEANUP_THRESHOLD) {
            return;
        }

        Iterator<Map.Entry<String, Long>> iterator = processed.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (now - entry.getValue() > ttlMillis) {
                iterator.remove();
            }
        }
    }
}
