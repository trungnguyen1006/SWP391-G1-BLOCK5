-- 1. Thêm cột 'excerpt'
ALTER TABLE blog ADD COLUMN excerpt VARCHAR(500) NULL;

-- 2. Thêm cột 'content'
ALTER TABLE blog ADD COLUMN content LONGTEXT NULL;

-- 3. Cập nhật dữ liệu cho cột 'excerpt' nếu nó chưa có (optional backfill)
UPDATE blog 
SET excerpt = CONCAT('Bài viết: ', title) 
WHERE excerpt IS NULL;


