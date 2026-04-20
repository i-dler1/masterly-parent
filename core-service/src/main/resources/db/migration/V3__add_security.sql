-- Добавляем роль в masters (если ещё нет)
ALTER TABLE masters ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'MASTER';

-- Добавляем роль в clients
ALTER TABLE clients ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'CLIENT';

-- Индекс для быстрого поиска по email
CREATE INDEX IF NOT EXISTS idx_masters_email ON masters(email);
CREATE INDEX IF NOT EXISTS idx_clients_email ON clients(email);