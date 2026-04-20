package com.masterly.core.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Slf4j
@Configuration
public class DatabaseMigrationConfig {

    @Value("${spring.datasource.url}")
    private String appUrl;

    @Value("${spring.datasource.username}")
    private String appUser;

    @Value("${spring.datasource.password}")
    private String appPassword;

    @Value("${database.admin.url:jdbc:postgresql://localhost:5432/postgres}")
    private String adminUrl;

    @Value("${database.admin.username:postgres}")
    private String adminUser;

    @Value("${database.admin.password:postgres}")
    private String adminPassword;

    @PostConstruct
    public void initDatabase() {
        log.info("=== Starting automatic database initialization ===");

        // 1. Создаём БД и пользователя
        createDatabaseAndUser();

        // 2. Ждём 2 секунды для применения прав
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        // 3. Запускаем Flyway миграции
        log.info("Running Flyway migrations...");
        Flyway flyway = Flyway.configure()
                .dataSource(appUrl, appUser, appPassword)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
        log.info("=== Database initialization completed! ===");
    }

    private void createDatabaseAndUser() {
        String dbName = extractDatabaseName(appUrl);

        try (Connection conn = DriverManager.getConnection(adminUrl, adminUser, adminPassword);
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(true);

            // Создаём пользователя
            try {
                stmt.execute(String.format("CREATE USER %s WITH PASSWORD '%s'", appUser, appPassword));
                log.info("✅ User '{}' created", appUser);
            } catch (Exception e) {
                if (e.getMessage().contains("already exists")) {
                    log.info("ℹ️ User '{}' already exists", appUser);
                } else {
                    log.warn("User creation: {}", e.getMessage());
                }
            }

            // Создаём базу данных
            try {
                stmt.execute(String.format("CREATE DATABASE %s", dbName));
                log.info("✅ Database '{}' created", dbName);
            } catch (Exception e) {
                if (e.getMessage().contains("already exists")) {
                    log.info("ℹ️ Database '{}' already exists", dbName);
                } else {
                    log.warn("Database creation: {}", e.getMessage());
                }
            }

            // Даём права на БД
            try {
                stmt.execute(String.format("GRANT ALL PRIVILEGES ON DATABASE %s TO %s", dbName, appUser));
                log.info("✅ Privileges granted on database");
            } catch (Exception e) {
                log.warn("Grant privileges: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to create database/user: {}", e.getMessage());
        }

        // Отдельное подключение для прав на схему
        grantSchemaPrivileges(dbName);
    }

    private void grantSchemaPrivileges(String dbName) {
        String url = String.format("jdbc:postgresql://localhost:5432/%s", dbName);

        try (Connection conn = DriverManager.getConnection(url, adminUser, adminPassword);
             Statement stmt = conn.createStatement()) {

            // Даём права на схему public
            stmt.execute(String.format("GRANT ALL ON SCHEMA public TO %s", appUser));
            stmt.execute(String.format("ALTER SCHEMA public OWNER TO %s", appUser));
            log.info("✅ Schema privileges granted");

        } catch (Exception e) {
            log.warn("Failed to grant schema privileges: {}", e.getMessage());
        }
    }

    private String extractDatabaseName(String url) {
        int lastSlash = url.lastIndexOf('/');
        String dbName = url.substring(lastSlash + 1);
        int questionMark = dbName.indexOf('?');
        if (questionMark != -1) {
            dbName = dbName.substring(0, questionMark);
        }
        return dbName;
    }
}