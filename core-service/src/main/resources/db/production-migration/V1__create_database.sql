-- flyway:executeInTransaction=false

-- Создаём базу данных (если не существует)
DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'masterly_db') THEN
            CREATE DATABASE masterly_db;
        END IF;
    END
$$;

-- Создаём пользователя, если его нет
DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'masterly_user') THEN
            CREATE USER masterly_user WITH PASSWORD 'masterly_password';
        END IF;
    END
$$;

-- Даём права
GRANT ALL PRIVILEGES ON DATABASE masterly_db TO masterly_user;