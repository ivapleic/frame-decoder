-- Table for the received transaction messages.
CREATE TABLE IF NOT EXISTS transactions (
                                            id               INTEGER PRIMARY KEY AUTOINCREMENT,
                                            transaction_type TEXT NOT NULL,
                                            transaction_id   TEXT NOT NULL,
                                            amount           TEXT NOT NULL,
                                            currency         TEXT NOT NULL
);