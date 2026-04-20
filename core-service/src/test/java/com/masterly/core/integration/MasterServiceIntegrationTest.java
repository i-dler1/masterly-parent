//package com.masterly.core.integration;
//
//import com.masterly.core.CoreServiceApplication;
//import com.masterly.core.dto.MasterDto;
//import com.masterly.core.service.MasterService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Testcontainers
//@SpringBootTest(classes = CoreServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class MasterServiceIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
//            .withDatabaseName("testdb")
//            .withUsername("test")
//            .withPassword("test");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//        registry.add("spring.flyway.enabled", () -> "false");
//        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
//    }
//
//    @Autowired
//    private MasterService masterService;
//
//    @Test
//    void contextLoads() {
//        assertNotNull(masterService);
//    }
//
//    static {
//    }
//}