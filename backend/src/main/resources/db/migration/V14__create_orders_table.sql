CREATE TABLE orders (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total         DECIMAL(12,2) NOT NULL,
    delivery_cost DECIMAL(12,2) NOT NULL DEFAULT 0,
    status        VARCHAR(50)   NOT NULL DEFAULT 'pending',
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);
