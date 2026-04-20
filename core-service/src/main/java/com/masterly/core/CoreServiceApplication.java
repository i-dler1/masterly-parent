package com.masterly.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс микросервиса core-service.
 * <p>
 * Отвечает за бизнес-логику и взаимодействие с базой данных.
 * Предоставляет REST API для управления мастерами, клиентами, услугами, материалами и записями.
 * </p>
 */
@SpringBootApplication
public class CoreServiceApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(CoreServiceApplication.class, args);
    }
}