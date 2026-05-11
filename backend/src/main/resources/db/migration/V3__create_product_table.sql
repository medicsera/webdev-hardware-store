CREATE TABLE products (
  id          BIGSERIAL PRIMARY KEY,
  name        VARCHAR(255) NOT NULL,
  description TEXT,
  price_cents BIGINT NOT NULL,
  image_url   VARCHAR(512)
);