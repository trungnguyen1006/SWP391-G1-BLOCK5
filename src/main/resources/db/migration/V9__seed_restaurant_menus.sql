-- Seed simple menu items for existing restaurants
INSERT INTO menu (restaurant_id, item_name, image_url)
SELECT r.restaurant_id, CONCAT('Món đặc biệt của ', r.name), r.image_url
FROM restaurant r
WHERE NOT EXISTS (
  SELECT 1 FROM menu m WHERE m.restaurant_id = r.restaurant_id
);

