CREATE TABLE loans (
    id UUID PRIMARY KEY,
    borrower_id UUID NOT NULL,
    principal NUMERIC(18,2) NOT NULL,
    annual_interest_rate NUMERIC(7,4) NOT NULL,
    term_months INTEGER NOT NULL,
    purpose VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_borrower FOREIGN KEY (borrower_id) REFERENCES users(id)
);

CREATE INDEX idx_loans_borrower_id ON loans(borrower_id);
