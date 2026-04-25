ALTER TABLE restaurant
  ADD COLUMN price_range VARCHAR(64) NULL,
  ADD COLUMN promotion_text VARCHAR(255) NULL;

-- Backfill simple price/promo for existing seed
UPDATE restaurant SET price_range = '150.000 - 400.000đ/người' WHERE price_range IS NULL;
UPDATE restaurant SET promotion_text = 'Giảm 10% hóa đơn cuối tuần' WHERE promotion_text IS NULL;

