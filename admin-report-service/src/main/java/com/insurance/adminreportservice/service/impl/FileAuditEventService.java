package com.insurance.adminreportservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.adminreportservice.service.AuditEventService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Writes audit events to a local JSONL file.
 */
@Slf4j
@Service
public class FileAuditEventService implements AuditEventService {

    private final ObjectMapper objectMapper;
    private final Path auditFile;

    public FileAuditEventService(ObjectMapper objectMapper,
                                 @Value("${audit.events.file:logs/audit-events.jsonl}") String auditFilePath) {
        this.objectMapper = objectMapper;
        this.auditFile = Path.of(auditFilePath);
    }

    @Override
    public void record(String eventType, Object eventPayload) {
        AuditRecord record = new AuditRecord(eventType, LocalDateTime.now(), eventPayload);
        try {
            ensureParentDirectory();
            String json = objectMapper.writeValueAsString(record);
            Files.writeString(auditFile, json + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            log.error("Failed to persist audit event type={}", eventType, exception);
        }
    }

    private void ensureParentDirectory() throws IOException {
        Path parent = auditFile.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private record AuditRecord(String eventType, LocalDateTime recordedAt, Object payload) {
    }
}
