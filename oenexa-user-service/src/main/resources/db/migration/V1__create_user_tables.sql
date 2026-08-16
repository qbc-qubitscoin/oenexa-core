CREATE TABLE IF NOT EXISTS user_profiles (
    user_id      CHAR(36)     NOT NULL PRIMARY KEY,
    first_name   VARCHAR(100),
    last_name    VARCHAR(100),
    date_of_birth DATE,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city         VARCHAR(100),
    country      VARCHAR(100),
    postal_code  VARCHAR(20),
    preferences  TEXT,
    kyc_level    VARCHAR(20)  NOT NULL DEFAULT 'NONE',
    version      BIGINT       NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
