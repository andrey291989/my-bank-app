CREATE TABLE IF NOT EXISTS cash_schema.cash_transactions (
                                                             id BIGSERIAL PRIMARY KEY,
                                                             user_login VARCHAR(50) NOT NULL,
    amount INTEGER NOT NULL,
    action VARCHAR(10) NOT NULL CHECK (action IN ('PUT', 'GET')),
    balance_before INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_transactions_user_login ON cash_schema.cash_transactions(user_login);
CREATE INDEX idx_transactions_created_at ON cash_schema.cash_transactions(created_at);