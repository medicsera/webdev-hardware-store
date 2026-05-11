CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id   BIGINT,
    product_name VARCHAR(512)  NOT NULL,
    price        DECIMAL(12,2) NOT NULL,
    quantity     INT           NOT NULL,
    image_url    VARCHAR(1024)
);
