CREATE TABLE IF NOT EXISTS transfer_schema.transfer_transactions (
                                                                     id BIGSERIAL PRIMARY KEY,
                                                                     from_login VARCHAR(50) NOT NULL,
    to_login VARCHAR(50) NOT NULL,
    amount INTEGER NOT NULL,
    from_balance_before INTEGER,
    from_balance_after INTEGER,
    to_balance_before INTEGER,
    to_balance_after INTEGER,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED')),
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_transfers_from_login ON transfer_schema.transfer_transactions(from_login);
CREATE INDEX idx_transfers_to_login ON transfer_schema.transfer_transactions(to_login);
CREATE INDEX idx_transfers_created_at ON transfer_schema.transfer_transactions(created_at);
CREATE INDEX idx_transfers_status ON transfer_schema.transfer_transactions(status);