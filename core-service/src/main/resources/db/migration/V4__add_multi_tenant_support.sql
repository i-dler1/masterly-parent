CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (name, description)
VALUES ('ADMIN', 'Полный доступ ко всем данным системы'),
       ('MASTER', 'Доступ только к своим клиентам и записям'),
       ('CLIENT', 'Доступ только к своим записям')
ON CONFLICT (name) DO NOTHING;

ALTER TABLE masters
ADD COLUMN IF NOT EXISTS role_id BIGINT REFERENCES roles(id),
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;

UPDATE masters SET role_id = (SELECT id FROM roles WHERE name = 'MASTER');

ALTER TABLE masters
ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE clients
ADD COLUMN IF NOT EXISTS master_id
BIGINT REFERENCES masters(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS created_by
BIGINT REFERENCES masters(id);

ALTER TABLE services
    ADD COLUMN IF NOT EXISTS master_id
    BIGINT REFERENCES masters(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS is_global
BOOLEAN DEFAULT FALSE;

ALTER TABLE materials
    ADD COLUMN IF NOT EXISTS master_id
    BIGINT REFERENCES masters(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_clients_master_id ON clients(master_id);
CREATE INDEX IF NOT EXISTS idx_services_master_id ON services(master_id);
CREATE INDEX IF NOT EXISTS idx_materials_master_id ON materials(master_id);
CREATE INDEX IF NOT EXISTS idx_appointments_master_id ON appointments(master_id);

INSERT INTO masters (email, password_hash, full_name, role_id, is_active)
VALUES ('admin@masterly.com',
        'admin123',
        'Системный администратор',
        (SELECT id FROM roles WHERE name = 'ADMIN'),
        TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO masters (email, password_hash, full_name, role_id, is_active)
VALUES ('client@masterly.com',
        'client123',
        'Тестовый Клиент',
        (SELECT id FROM roles WHERE name = 'CLIENT'),
        TRUE)
ON CONFLICT (email) DO NOTHING;

COMMENT ON TABLE roles IS 'Роли пользователей системы';
COMMENT ON COLUMN masters.role_id IS 'Роль мастера';
COMMENT ON COLUMN masters.is_active IS 'Активен ли мастер (может ли входить в систему)';
COMMENT ON COLUMN clients.master_id IS 'Какой мастер ведет этого клиента';
COMMENT ON COLUMN services.is_global IS 'Глобальная услуга (видна всем мастерам)';