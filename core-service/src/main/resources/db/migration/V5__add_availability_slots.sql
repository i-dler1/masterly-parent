-- Таблица доступных слотов времени мастера
CREATE TABLE IF NOT EXISTS availability_slots (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  master_id BIGINT NOT NULL REFERENCES masters(id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_booked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_availability_master_date ON availability_slots(master_id, slot_date);
CREATE INDEX IF NOT EXISTS idx_availability_service ON availability_slots(service_id);

COMMENT ON TABLE availability_slots IS 'Слоты доступного времени мастеров';
COMMENT ON COLUMN availability_slots.is_booked IS 'Забронирован ли слот';