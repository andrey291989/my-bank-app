-- Создание схем для микросервисов (Database per Service pattern)
CREATE SCHEMA IF NOT EXISTS accounts_schema;
CREATE SCHEMA IF NOT EXISTS cash_schema;
CREATE SCHEMA IF NOT EXISTS transfer_schema;
CREATE SCHEMA IF NOT EXISTS notifications_schema;

-- Предоставление прав
GRANT ALL PRIVILEGES ON SCHEMA accounts_schema TO bank_user;
GRANT ALL PRIVILEGES ON SCHEMA cash_schema TO bank_user;
GRANT ALL PRIVILEGES ON SCHEMA transfer_schema TO bank_user;
GRANT ALL PRIVILEGES ON SCHEMA notifications_schema TO bank_user;

-- Установка search_path по умолчанию
ALTER ROLE bank_user SET search_path TO accounts_schema, cash_schema, transfer_schema, notifications_schema, public;