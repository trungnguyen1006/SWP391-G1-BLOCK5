-- Seed restaurants if empty (handles cases where schema was baselined)
INSERT INTO restaurant (name, image_url, cuisine_type, opening_hours)
SELECT * FROM (
    SELECT 'La Bella Italia' AS name, 'https://images.unsplash.com/photo-1520201163981-8aa2f46f4000?q=80&w=1200&auto=format&fit=crop' AS image_url, 'Italian' AS cuisine_type, '10:00 - 22:00' AS opening_hours
    UNION ALL SELECT 'Sakura Sushi', 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop', 'Japanese', '11:00 - 21:30'
    UNION ALL SELECT 'El Toro Loco', 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?q=80&w=1200&auto=format&fit=crop', 'Mexican', '10:30 - 23:00'
    UNION ALL SELECT 'Bangkok Spice', 'https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1200&auto=format&fit=crop', 'Thai', '11:00 - 22:00'
    UNION ALL SELECT 'Le Petit Paris', 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?q=80&w=1200&auto=format&fit=crop', 'French', '07:30 - 22:00'
    UNION ALL SELECT 'Harbor Grill', 'https://images.unsplash.com/photo-1559339352-2f88a965b38a?q=80&w=1200&auto=format&fit=crop', 'Seafood', '10:00 - 22:00'
    UNION ALL SELECT 'Green Garden', 'https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?q=80&w=1200&auto=format&fit=crop', 'Vegetarian', '08:00 - 20:00'
    UNION ALL SELECT 'Steakhouse Prime', 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop', 'Steakhouse', '12:00 - 23:00'
    UNION ALL SELECT 'Curry Corner', 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop', 'Indian', '11:00 - 21:00'
    UNION ALL SELECT 'Mandarin Palace', 'https://images.unsplash.com/photo-1544025162-84f9a6d0a971?q=80&w=1200&auto=format&fit=crop', 'Chinese', '10:00 - 22:00'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM restaurant);


