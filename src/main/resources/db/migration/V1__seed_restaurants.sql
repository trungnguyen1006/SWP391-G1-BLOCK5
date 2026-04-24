-- Ensure table exists (for safety when ddl-auto=none)
CREATE TABLE IF NOT EXISTS restaurant (
    restaurant_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    image_url VARCHAR(1000),
    cuisine_type VARCHAR(100),
    opening_hours VARCHAR(255),
    deleted_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed 10 restaurants with Unsplash images
INSERT INTO restaurant (name, image_url, cuisine_type, opening_hours)
VALUES
('La Bella Italia', 'https://images.unsplash.com/photo-1520201163981-8aa2f46f4000?q=80&w=1200&auto=format&fit=crop', 'Italian', '10:00 - 22:00'),
('Sakura Sushi', 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop', 'Japanese', '11:00 - 21:30'),
('El Toro Loco', 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?q=80&w=1200&auto=format&fit=crop', 'Mexican', '10:30 - 23:00'),
('Bangkok Spice', 'https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1200&auto=format&fit=crop', 'Thai', '11:00 - 22:00'),
('Le Petit Paris', 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?q=80&w=1200&auto=format&fit=crop', 'French', '07:30 - 22:00'),
('Harbor Grill', 'https://images.unsplash.com/photo-1559339352-2f88a965b38a?q=80&w=1200&auto=format&fit=crop', 'Seafood', '10:00 - 22:00'),
('Green Garden', 'https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?q=80&w=1200&auto=format&fit=crop', 'Vegetarian', '08:00 - 20:00'),
('Steakhouse Prime', 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop', 'Steakhouse', '12:00 - 23:00'),
('Curry Corner', 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop', 'Indian', '11:00 - 21:00'),
('Mandarin Palace', 'https://images.unsplash.com/photo-1544025162-84f9a6d0a971?q=80&w=1200&auto=format&fit=crop', 'Chinese', '10:00 - 22:00');


