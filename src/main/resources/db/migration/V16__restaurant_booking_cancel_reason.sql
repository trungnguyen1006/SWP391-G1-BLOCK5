-- V16: Thêm cột lý do hủy vào bảng restaurant_booking
ALTER TABLE restaurant_booking
    ADD COLUMN cancel_reason VARCHAR(500) NULL COMMENT 'Lý do hủy đặt bàn'
    AFTER special_request;
