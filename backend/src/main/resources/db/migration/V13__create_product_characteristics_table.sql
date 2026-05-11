CREATE TABLE product_characteristics (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    char_name  VARCHAR(255) NOT NULL,
    char_value VARCHAR(1024),
    PRIMARY KEY (product_id, char_name)
);
