ALTER TABLE sub_catalogs DROP CONSTRAINT IF EXISTS sub_catalogs_slug_key;
ALTER TABLE sub_catalogs ADD CONSTRAINT sub_catalogs_slug_catalog_unique UNIQUE (catalog_id, slug);
