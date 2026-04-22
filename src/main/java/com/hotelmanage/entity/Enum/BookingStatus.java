package com.hotelmanage.entity.Enum;

public enum BookingStatus {
    PENDING,               // Chờ thanh toán
    CONFIRMED,             // Đã thanh toán / xác nhận
    CHECKED_IN,            // Khách đã nhận phòng
    CHECKED_OUT,           // Khách đã trả phòng
    CANCELLED_PERMANENTLY  // Đã hủy
}
