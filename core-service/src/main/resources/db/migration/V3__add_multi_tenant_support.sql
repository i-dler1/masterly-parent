-- Составные индексы для ускорения фильтрации по master_id
CREATE INDEX IF NOT EXISTS idx_appointments_master_date ON appointments(master_id, appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_master_status ON appointments(master_id, status);
CREATE INDEX IF NOT EXISTS idx_clients_master_regular ON clients(master_id, is_regular);
CREATE INDEX IF NOT EXISTS idx_services_master_active ON services(master_id, is_active);
CREATE INDEX IF NOT EXISTS idx_materials_master_low_stock ON materials(master_id, quantity, min_quantity)
    WHERE quantity <= min_quantity;
CREATE INDEX IF NOT EXISTS idx_availability_slots_master_service ON availability_slots(master_id, service_id);
CREATE INDEX IF NOT EXISTS idx_availability_slots_master_booked ON availability_slots(master_id, is_booked);