ALTER TABLE orders
    ADD COLUMN delivery_method  VARCHAR(20)  NOT NULL DEFAULT 'pickup',
    ADD COLUMN delivery_address VARCHAR(512);
