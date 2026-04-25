ALTER TABLE menu
  ADD COLUMN price DECIMAL(12,0) NULL;

-- Backfill a default price for existing menu items
UPDATE menu SET price = 120000 WHERE price IS NULL;

