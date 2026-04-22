package com.hotelmanage.repository.restaurant;

import com.hotelmanage.entity.Enum.BookingShift;
import com.hotelmanage.entity.Enum.RestaurantBookingStatus;
import com.hotelmanage.entity.restaurant.RestaurantBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RestaurantBookingRepository extends JpaRepository<RestaurantBooking, Long> {

    // ─────────────────────────────────────────────────────────────────
    // Kiểm tra capacity: đếm booking PENDING + CONFIRMED cùng ca
    // → dùng trong service trước khi cho phép đặt bàn
    // ─────────────────────────────────────────────────────────────────
    @Query("""
            SELECT COUNT(b) FROM RestaurantBooking b
            WHERE b.restaurant.id   = :restaurantId
              AND b.bookingDate      = :date
              AND b.bookingShift     = :shift
              AND b.status          IN :statuses
            """)
    long countActiveBookings(@Param("restaurantId") Long restaurantId,
                             @Param("date")         LocalDate date,
                             @Param("shift")        BookingShift shift,
                             @Param("statuses")     List<RestaurantBookingStatus> statuses);

    // ─────────────────────────────────────────────────────────────────
    // Lịch sử đặt bàn của user (trang "Đặt bàn của tôi")
    // ─────────────────────────────────────────────────────────────────
    List<RestaurantBooking> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ─────────────────────────────────────────────────────────────────
    // Admin / Receptionist — toàn bộ booking, phân trang
    // ─────────────────────────────────────────────────────────────────
    Page<RestaurantBooking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Admin / Receptionist — filter theo status, phân trang
    Page<RestaurantBooking> findByStatusOrderByCreatedAtDesc(
            RestaurantBookingStatus status, Pageable pageable);

    // Admin / Receptionist — filter theo nhà hàng, phân trang
    Page<RestaurantBooking> findByRestaurantIdOrderByCreatedAtDesc(
            Long restaurantId, Pageable pageable);

    // Admin / Receptionist — filter status + nhà hàng, phân trang
    Page<RestaurantBooking> findByStatusAndRestaurantIdOrderByCreatedAtDesc(
            RestaurantBookingStatus status, Long restaurantId, Pageable pageable);

    // ─────────────────────────────────────────────────────────────────
    // Kiểm tra user đã có booking active cùng nhà hàng + ngày + ca chưa
    // → chặn đặt trùng
    // ─────────────────────────────────────────────────────────────────
    @Query("""
            SELECT COUNT(b) FROM RestaurantBooking b
            WHERE b.user.id        = :userId
              AND b.restaurant.id  = :restaurantId
              AND b.bookingDate    = :date
              AND b.bookingShift   = :shift
              AND b.status        IN :statuses
            """)
    long countUserActiveBookings(@Param("userId")       Long userId,
                                 @Param("restaurantId") Long restaurantId,
                                 @Param("date")         LocalDate date,
                                 @Param("shift")        BookingShift shift,
                                 @Param("statuses")     List<RestaurantBookingStatus> statuses);

    // ─────────────────────────────────────────────────────────────────
    // Dashboard: đếm theo trạng thái
    // ─────────────────────────────────────────────────────────────────
    Long countByStatus(RestaurantBookingStatus status);
}
