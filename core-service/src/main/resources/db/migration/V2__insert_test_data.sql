DELETE FROM service_materials;
DELETE FROM appointments;
DELETE FROM materials;
DELETE FROM services;
DELETE FROM clients;
DELETE FROM masters;

INSERT INTO masters (email, password_hash, full_name)
VALUES ('test@masterly.com', '123', 'Тестовый Мастер');

INSERT INTO clients (master_id, full_name, phone, email)
VALUES (1, 'Анна Смирнова', '+7 (999) 123-45-67', 'anna@example.com');

INSERT INTO services (master_id, name, description, duration_minutes, price, category)
VALUES (1, 'Классическое наращивание', 'Поресничное наращивание', 120, 2500.00, 'наращивание');

INSERT INTO materials (master_id, name, unit, quantity, min_quantity, price_per_unit, supplier)
VALUES (1, 'Клей для ресниц', 'мл', 10.00, 2.00, 1500.00, 'LashPro');

INSERT INTO service_materials (service_id, material_id, quantity_used)
VALUES (1, 1, 2.00);