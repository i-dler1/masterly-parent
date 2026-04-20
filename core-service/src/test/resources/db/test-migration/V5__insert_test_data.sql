-- Очистка таблиц
TRUNCATE TABLE service_materials CASCADE;
TRUNCATE TABLE appointments CASCADE;
TRUNCATE TABLE availability_slots CASCADE;
TRUNCATE TABLE services CASCADE;
TRUNCATE TABLE materials CASCADE;
TRUNCATE TABLE clients CASCADE;
TRUNCATE TABLE masters CASCADE;

-- Сброс последовательностей
ALTER SEQUENCE IF EXISTS masters_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS clients_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS materials_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS services_id_seq RESTART WITH 1;

-- Мастер (пароль: 123)
INSERT INTO masters (email, password_hash, full_name, phone, business_name, specialization, role, is_active, created_at,
                     updated_at)
VALUES ('test@masterly.com',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        'Анна Стилист',
        '+375291112233',
        'Beauty Studio',
        'Парикмахер-колорист',
        'MASTER',
        true,
        NOW(),
        NOW());

-- Клиенты
INSERT INTO clients (master_id, full_name, phone, email, role, created_at, updated_at)
VALUES (1, 'Елена Смирнова', '+375331234567', 'elena@mail.com', 'CLIENT', NOW(), NOW()),
       (1, 'Ольга Иванова', '+375442345678', 'olga@mail.com', 'CLIENT', NOW(), NOW()),
       (1, 'Марина Петрова', '+375293456789', 'marina@mail.com', 'CLIENT', NOW(), NOW());

-- Материалы
INSERT INTO materials (master_id, name, unit, quantity, min_quantity, price_per_unit, last_updated)
VALUES (1, 'Краска Estel', 'мл', 500, 50, 2.5, NOW()),
       (1, 'Оксидант 3%', 'мл', 1000, 100, 0.5, NOW()),
       (1, 'Оксидант 6%', 'мл', 800, 100, 0.5, NOW()),
       (1, 'Шампунь', 'мл', 2000, 200, 1.0, NOW()),
       (1, 'Маска для волос', 'мл', 1000, 100, 3.0, NOW());

-- Услуги
INSERT INTO services (master_id, name, description, duration_minutes, price, is_active, created_at, updated_at)
VALUES (1, 'Стрижка женская', 'Модельная стрижка с укладкой', 60, 35.0, true, NOW(), NOW()),
       (1, 'Окрашивание корней', 'Окрашивание отросших корней', 90, 50.0, true, NOW(), NOW()),
       (1, 'Сложное окрашивание', 'AirTouch, балаяж, мелирование', 180, 120.0, true, NOW(), NOW()),
       (1, 'Укладка', 'Укладка волос', 45, 20.0, true, NOW(), NOW());

-- Связи услуга-материал
INSERT INTO service_materials (service_id, material_id, quantity_used)
VALUES (2, 1, 40.0), -- Окрашивание корней: 40мл краски
       (2, 2, 40.0), -- Окрашивание корней: 40мл оксиданта 3%
       (3, 1, 80.0), -- Сложное окрашивание: 80мл краски
       (3, 3, 80.0); -- Сложное окрашивание: 80мл оксиданта 6%