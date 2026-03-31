-- Добавляем поле role
ALTER TABLE masters
    ADD COLUMN IF NOT EXISTS role VARCHAR(50) NOT NULL DEFAULT 'MASTER';

-- Обновляем password_hash для test@masterly.com (пароль: 123 в открытом виде)
UPDATE masters
SET password_hash = '123'
WHERE email = 'test@masterly.com';

-- Создаем индекс для быстрого поиска по email
CREATE INDEX IF NOT EXISTS idx_masters_email ON masters(email);