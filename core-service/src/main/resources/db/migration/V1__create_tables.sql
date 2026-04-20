-- Таблица мастеров
CREATE TABLE IF NOT EXISTS masters (
                                       id BIGSERIAL PRIMARY KEY,
                                       email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    business_name VARCHAR(200),
    specialization TEXT,
    avatar_url VARCHAR(500),
    role VARCHAR(20) DEFAULT 'MASTER',
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- Таблица клиентов
CREATE TABLE IF NOT EXISTS clients (
                                       id BIGSERIAL PRIMARY KEY,
                                       master_id BIGINT NOT NULL REFERENCES masters(id) ON DELETE CASCADE,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    instagram VARCHAR(100),
    telegram VARCHAR(100),
    birth_date DATE,
    notes TEXT,
    is_regular BOOLEAN DEFAULT false,
    role VARCHAR(20) DEFAULT 'CLIENT',
    created_by BIGINT REFERENCES masters(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                                                                                            );

-- Таблица услуг
CREATE TABLE IF NOT EXISTS services (
                                        id BIGSERIAL PRIMARY KEY,
                                        master_id BIGINT NOT NULL REFERENCES masters(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                                                                                             );

-- Таблица материалов
CREATE TABLE IF NOT EXISTS materials (
                                         id BIGSERIAL PRIMARY KEY,
                                         master_id BIGINT NOT NULL REFERENCES masters(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 0,
    min_quantity DECIMAL(10,2),
    price_per_unit DECIMAL(10,2),
    supplier VARCHAR(200),
    notes TEXT,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                                                                                              );

-- Таблица слотов доступности
CREATE TABLE IF NOT EXISTS availability_slots (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  master_id BIGINT NOT NULL REFERENCES masters(id) ON DELETE CASCADE,
    service_id BIGINT REFERENCES services(id) ON DELETE SET NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_booked BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблица записей
CREATE TABLE IF NOT EXISTS appointments (
                                            id BIGSERIAL PRIMARY KEY,
                                            master_id BIGINT NOT NULL REFERENCES masters(id) ON DELETE CASCADE,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(id),
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'))
    );

-- Таблица связей услуга-материал
CREATE TABLE IF NOT EXISTS service_materials (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    material_id BIGINT NOT NULL REFERENCES materials(id) ON DELETE CASCADE,
    quantity_used DECIMAL(10,3) NOT NULL,
    notes TEXT,
    UNIQUE(service_id, material_id)
    );

-- Индексы
CREATE INDEX IF NOT EXISTS idx_clients_master_id ON clients(master_id);
CREATE INDEX IF NOT EXISTS idx_services_master_id ON services(master_id);
CREATE INDEX IF NOT EXISTS idx_materials_master_id ON materials(master_id);
CREATE INDEX IF NOT EXISTS idx_appointments_master_id ON appointments(master_id);
CREATE INDEX IF NOT EXISTS idx_appointments_client_id ON appointments(client_id);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_service_materials_service ON service_materials(service_id);
CREATE INDEX IF NOT EXISTS idx_service_materials_material ON service_materials(material_id);
CREATE INDEX IF NOT EXISTS idx_availability_slots_master_date ON availability_slots(master_id, slot_date);