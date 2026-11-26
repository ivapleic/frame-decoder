-- Table for the received transaction messages.
-- One row per message, id keeps the order in which they were received.
CREATE TABLE IF NOT EXISTS transactions (
                                            id               INTEGER PRIMARY KEY AUTOINCREMENT,
                                            transaction_type TEXT NOT NULL,
                                            transaction_id   TEXT NOT NULL,
                                            amount           TEXT NOT NULL,
                                            currency         TEXT NOT NULL
);