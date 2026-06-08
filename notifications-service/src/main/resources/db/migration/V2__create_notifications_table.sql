CREATE TABLE IF NOT EXISTS notifications_schema.notifications (
                                                                  id BIGSERIAL PRIMARY KEY,
                                                                  user_login VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    delivery_method VARCHAR(20) NOT NULL CHECK (delivery_method IN ('LOG', 'EMAIL')),
    delivery_status VARCHAR(20) NOT NULL CHECK (delivery_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP
    );

CREATE INDEX idx_notifications_user_login ON notifications_schema.notifications(user_login);
CREATE INDEX idx_notifications_created_at ON notifications_schema.notifications(created_at);
CREATE INDEX idx_notifications_delivery_status ON notifications_schema.notifications(delivery_status);