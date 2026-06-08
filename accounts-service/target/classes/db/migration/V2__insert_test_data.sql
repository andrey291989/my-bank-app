INSERT INTO accounts_schema.accounts (login, full_name, birthdate, sum, created_at)
VALUES
    ('ivanov', 'Иванов Иван', '1990-05-15', 5000, CURRENT_TIMESTAMP),
    ('petrov', 'Петров Петр', '1985-08-20', 3000, CURRENT_TIMESTAMP),
    ('sidorov', 'Сидоров Сидор', '1995-03-10', 2000, CURRENT_TIMESTAMP),
    ('smirnov', 'Смирнов Алексей', '2000-12-01', 1000, CURRENT_TIMESTAMP)
    ON CONFLICT (login) DO NOTHING;