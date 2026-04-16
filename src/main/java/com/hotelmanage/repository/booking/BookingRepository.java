package com.hotelmanage.repository.booking;


import com.hotelmanage.entity.Enum.BookingStatus;
import com.hotelmanage.entity.booking.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Tìm tất cả booking của user
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :expiryTime")
    List<Booking> findExpiredPendingBookings(@Param("status") BookingStatus status,
                                             @Param("expiryTime") LocalDateTime expiryTime);

    // Tìm tất cả booking với phân trang
    @Query("SELECT b FROM Booking b ORDER BY b.createdAt DESC")
    Page<Booking> findAllBookingsWithPagination(Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE CAST(b.bookingId AS string) LIKE %:bookingId% ORDER BY b.createdAt DESC")
    Page<Booking> findBookingsByBookingIdContaining(@Param("bookingId") String bookingId, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Booking b")
    Long countTotalBookings();

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND YEAR(b.createdAt) = :year AND MONTH(b.createdAt) = :month")
    BigDecimal calculateMonthlyRevenue(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' " +
            "AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    BigDecimal calculateRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);


}
