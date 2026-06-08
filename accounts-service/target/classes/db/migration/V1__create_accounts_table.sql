CREATE SCHEMA IF NOT EXISTS accounts_schema;

CREATE TABLE accounts_schema.accounts (
                                          id BIGSERIAL PRIMARY KEY,
                                          login VARCHAR(50) NOT NULL UNIQUE,
                                          full_name VARCHAR(100) NOT NULL,
                                          birthdate DATE NOT NULL,
                                          sum INTEGER NOT NULL DEFAULT 0,
                                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP
);

CREATE INDEX idx_accounts_login ON accounts_schema.accounts(login);