package com.masterly.core.config;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;

public final class TestcontainersSingleton {

    private static final PostgreSQLContainer<?> INSTANCE;

    static {
        try {
            INSTANCE = new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
//                    .withReuse(true);
                    .withReuse(false);
            INSTANCE.start();

            migrate();

            Runtime.getRuntime().addShutdownHook(new Thread(INSTANCE::stop));
        } catch (Exception e) {
            System.err.println(">>> ОШИБКА в TestcontainersSingleton: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static PostgreSQLContainer<?> getInstance() {
        return INSTANCE;
    }

    public static void migrate() {
        Flyway.configure()
                .dataSource(INSTANCE.getJdbcUrl(), INSTANCE.getUsername(), INSTANCE.getPassword())
                .locations("classpath:db/migration", "classpath:db/test-migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load()
                .migrate();
    }

    public static void clean() {
        Flyway.configure()
                .dataSource(INSTANCE.getJdbcUrl(), INSTANCE.getUsername(), INSTANCE.getPassword())
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load()
                .clean();
        migrate();
    }
}