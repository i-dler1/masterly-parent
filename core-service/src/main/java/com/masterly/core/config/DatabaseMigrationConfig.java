package com.masterly.core.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Конфигурация для автоматического создания базы данных и выполнения миграций Flyway.
 * <p>
 * Используется ручное управление Flyway вместо spring.flyway.*
 * Причина: необходимо сначала создать БД через админское подключение к postgres,
 * затем выполнить миграции. Стандартный Flyway не умеет создавать БД.
 * <p>
 * <b>Не выполняется в тестовом профиле (@Profile("!test")).</b>
 */
@Slf4j
@Configuration
@Profile("!test")
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


    /**
     * Инициализация базы данных при старте контекста.
     * <p>
     * Этап 1: создание БД через админское подключение (выполняется только V1).<br>
     * Этап 2: выполнение всех миграций через рабочее подключение.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void initDatabase() {
        log.info("Creating database...");
        Flyway.configure()
                .dataSource(adminUrl, adminUser, adminPassword)
                .locations("classpath:db/production-migration", "classpath:db/migration")
                .target("1")
                .load()
                .migrate();

        log.info("Applying migrations...");
        Flyway.configure()
                .dataSource(appUrl, appUser, appPassword)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        log.info("Database ready!");
    }
}