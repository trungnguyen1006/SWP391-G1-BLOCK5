-- =====================================================================
-- V15: Restaurant Booking Feature
--   1. Fix booking ENUM (thêm CHECKED_IN, CHECKED_OUT)
--   2. Thêm max_tables + 4 cột ca vào bảng restaurant
--   3. Seed ca dựa theo opening_hours hiện có
--   4. Tạo bảng restaurant_booking
-- =====================================================================


-- =====================================================================
-- 1. FIX: Bổ sung CHECKED_IN & CHECKED_OUT vào booking.status
--    (Java enum có 5 giá trị nhưng DB chỉ có 3)
-- =====================================================================
ALTER TABLE booking
    MODIFY COLUMN status
        ENUM('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED_PERMANENTLY')
        NOT NULL;


-- =====================================================================
-- 2. Thêm cột vào bảng restaurant
--    - max_tables  : sức chứa tối đa (số booking) mỗi ca
--    - has_morning : Ca sáng   07:00 – 10:00
--    - has_lunch   : Ca trưa   11:00 – 14:00
--    - has_afternoon: Ca chiều  14:00 – 17:00
--    - has_dinner  : Ca tối    18:00 – 22:00
-- =====================================================================
ALTER TABLE restaurant
    ADD COLUMN max_tables    INT     NOT NULL DEFAULT 10    COMMENT 'Số booking tối đa mỗi ca',
    ADD COLUMN has_morning   BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ca sáng 07:00-10:00',
    ADD COLUMN has_lunch     BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ca trưa 11:00-14:00',
    ADD COLUMN has_afternoon BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ca chiều 14:00-17:00',
    ADD COLUMN has_dinner    BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ca tối 18:00-22:00';


-- =====================================================================
-- 3. Seed ca dựa theo opening_hours của từng nhà hàng
-- =====================================================================

-- La Bella Italia      | 10:00 - 22:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 1;

-- Sakura Sushi         | 11:00 - 21:30 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 2;

-- El Toro Loco         | 10:30 - 23:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 3;

-- Bangkok Spice        | 11:00 - 22:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 4;

-- Le Petit Paris       | 07:30 - 22:00 → sáng, trưa, chiều, tối
UPDATE restaurant SET has_morning = TRUE, has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 5;

-- Harbor Grill         | 10:00 - 22:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 6;

-- Green Garden         | 08:00 - 20:00 → sáng, trưa, chiều, tối
UPDATE restaurant SET has_morning = TRUE, has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 7;

-- Steakhouse Prime     | 12:00 - 23:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 8;

-- Curry Corner         | 11:00 - 21:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 9;

-- Mandarin Palace      | 10:00 - 22:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 10;


-- =====================================================================
-- 4. Tạo bảng restaurant_booking
-- =====================================================================
CREATE TABLE restaurant_booking (
    booking_id       BIGINT      NOT NULL AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL,
    restaurant_id    BIGINT      NOT NULL,
    booking_date     DATE        NOT NULL,
    booking_shift    ENUM('SANG','TRUA','CHIEU','TOI') NOT NULL
                                 COMMENT 'SANG=07-10 | TRUA=11-14 | CHIEU=14-17 | TOI=18-22',
    number_of_guests INT         NOT NULL DEFAULT 1,
    special_request  VARCHAR(500)    NULL,
    status           ENUM('PENDING','CONFIRMED','CANCELLED')
                                 NOT NULL DEFAULT 'PENDING',
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                          ON UPDATE CURRENT_TIMESTAMP,
    deleted_at       DATETIME        NULL,

    PRIMARY KEY (booking_id),
    CONSTRAINT fk_rb_user       FOREIGN KEY (user_id)
        REFERENCES users(user_id),
    CONSTRAINT fk_rb_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES restaurant(restaurant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================================================================
-- 5. Indexes tối ưu hiệu suất truy vấn
-- =====================================================================

-- Kiểm tra capacity: nhà hàng + ngày + ca
CREATE INDEX idx_rb_restaurant_date_shift
    ON restaurant_booking (restaurant_id, booking_date, booking_shift);

-- Lịch sử đặt bàn của user
CREATE INDEX idx_rb_user
    ON restaurant_booking (user_id);

-- Filter theo trạng thái (admin/receptionist)
CREATE INDEX idx_rb_status
    ON restaurant_booking (status);

-- Xóa mềm
CREATE INDEX idx_rb_deleted_at
    ON restaurant_booking (deleted_at);
