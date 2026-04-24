-- Randomize menu prices per restaurant for demo
UPDATE menu m
JOIN restaurant r ON r.restaurant_id = m.restaurant_id
SET m.price =
  CASE
    WHEN r.name LIKE 'La Bella%' THEN 180000
    WHEN r.name LIKE 'Sakura%' THEN 240000
    WHEN r.name LIKE 'El Toro%' THEN 150000
    WHEN r.name LIKE 'Bangkok%' THEN 160000
    WHEN r.name LIKE 'Le Petit%' THEN 320000
    WHEN r.name LIKE 'Harbor%' THEN 450000
    WHEN r.name LIKE 'Green Garden%' THEN 140000
    WHEN r.name LIKE 'Steakhouse%' THEN 650000
    WHEN r.name LIKE 'Curry Corner%' THEN 130000
    WHEN r.name LIKE 'Mandarin%' THEN 170000
    ELSE 120000
  END;

