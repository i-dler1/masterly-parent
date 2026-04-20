-- Индекс для быстрого поиска по email
CREATE INDEX IF NOT EXISTS idx_masters_email ON masters(email);
CREATE INDEX IF NOT EXISTS idx_clients_email ON clients(email);