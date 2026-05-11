-- Fix products table: drop legacy columns, add quantity
ALTER TABLE products DROP COLUMN IF EXISTS price_cents;
ALTER TABLE products DROP COLUMN IF EXISTS image_url;
ALTER TABLE products ADD COLUMN IF NOT EXISTS quantity INT NOT NULL DEFAULT 0;
ALTER TABLE products ALTER COLUMN description SET NOT NULL;
