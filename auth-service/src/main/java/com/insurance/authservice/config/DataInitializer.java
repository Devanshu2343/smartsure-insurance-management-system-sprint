package com.insurance.authservice.config;

import com.insurance.authservice.entity.Role;
import com.insurance.authservice.entity.User;
import com.insurance.authservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initializeAdminUser() {
        return args -> {
            alignPhoneColumnIfNeeded();

            if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
                return;
            }

            User admin = new User();
            admin.setFullName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setPhoneNumber("9999999999");

            userRepository.save(admin);

            log.info("Admin user created: {}", adminEmail);
        };
    }

    private void alignPhoneColumnIfNeeded() {
        try {
            String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            if (databaseName == null || databaseName.isBlank()) {
                return;
            }

            boolean phoneExists = columnExists(databaseName, "phone");
            boolean phoneNumberExists = columnExists(databaseName, "phone_number");

            if (phoneExists && phoneNumberExists) {
                jdbcTemplate.execute("UPDATE users SET phone_number = COALESCE(phone_number, phone)");
                jdbcTemplate.execute("ALTER TABLE users DROP COLUMN phone");
                log.info("Aligned users table by dropping legacy phone column.");
                return;
            }

            if (phoneExists && !phoneNumberExists) {
                jdbcTemplate.execute("ALTER TABLE users CHANGE phone phone_number VARCHAR(255) NOT NULL");
                log.info("Renamed legacy phone column to phone_number.");
            }
        } catch (DataAccessException exception) {
            log.warn("Unable to align phone column automatically. You may need to drop/alter the users table.", exception);
        }
    }

    private boolean columnExists(String databaseName, String columnName) {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = ? AND table_name = 'users' AND column_name = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, databaseName, columnName);
        return count != null && count > 0;
    }
}
