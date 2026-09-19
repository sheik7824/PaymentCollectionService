CREATE TABLE scheduled_payments (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL,
    order_id VARCHAR(100) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_method_token VARCHAR(500) NOT NULL,
    schedule_expression VARCHAR(200) NOT NULL,
    next_run_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_scheduled_payment_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_scheduled_payments_next_run_date ON scheduled_payments(next_run_date);
