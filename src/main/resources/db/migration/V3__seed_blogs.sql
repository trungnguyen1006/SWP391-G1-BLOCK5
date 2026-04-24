-- Ensure table exists (if schema empty)
CREATE TABLE IF NOT EXISTS blog (
    blog_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    image_url VARCHAR(1000),
    deleted_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed 10 blogs with Unsplash images
INSERT INTO blog (title, image_url)
VALUES
('10 mẹo đặt phòng khách sạn tiết kiệm', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?q=80&w=1200&auto=format&fit=crop'),
('Trải nghiệm ẩm thực địa phương đáng thử', 'https://images.unsplash.com/photo-1470337458703-46ad1756a187?q=80&w=1200&auto=format&fit=crop'),
('Checklist chuẩn bị cho chuyến du lịch biển', 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=1200&auto=format&fit=crop'),
('Bí kíp săn vé máy bay giá tốt', 'https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?q=80&w=1200&auto=format&fit=crop'),
('Top 5 điểm đến lãng mạn mùa thu', 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=1200&auto=format&fit=crop'),
('Hành trình khám phá ẩm thực đường phố', 'https://images.unsplash.com/photo-1504754524776-8f4f37790ca0?q=80&w=1200&auto=format&fit=crop'),
('Kinh nghiệm du lịch một mình an toàn', 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?q=80&w=1200&auto=format&fit=crop'),
('Gợi ý lịch trình 3 ngày ở Đà Nẵng', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?q=80&w=1200&auto=format&fit=crop'),
('Những vật dụng cần có khi đi trekking', 'https://images.unsplash.com/photo-1526481280698-8fcc13fdc8c7?q=80&w=1200&auto=format&fit=crop'),
('Cách chọn khách sạn phù hợp với gia đình', 'https://images.unsplash.com/photo-1496412705862-e0088f16f791?q=80&w=1200&auto=format&fit=crop');


