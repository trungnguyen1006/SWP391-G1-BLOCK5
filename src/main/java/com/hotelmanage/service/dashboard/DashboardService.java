package com.hotelmanage.service.dashboard;

import com.hotelmanage.entity.Enum.BookingStatus;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import com.hotelmanage.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    /**
     * Lấy tất cả thống kê cho dashboard
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            Long totalRooms = roomRepository.countTotalRooms();
            stats.put("totalRooms", totalRooms != null ? totalRooms : 0L);
        } catch (Exception e) {
            stats.put("totalRooms", 0L);
            log.error("Error retrieving total rooms", e);
        }

        try {
            Long availableRooms = roomRepository.countAvailableRooms();
            stats.put("availableRooms", availableRooms != null ? availableRooms : 0L);
        } catch (Exception e) {
            stats.put("availableRooms", 0L);
            log.error("Error retrieving available rooms", e);
        }

        try {
            Long activeUsers = userRepository.countActiveUsers();
            stats.put("activeUsers", activeUsers != null ? activeUsers : 0L);
        } catch (Exception e) {
            stats.put("activeUsers", 0L);
            log.error("Error retrieving active users", e);
        }

        try {
            Long totalBookings = bookingRepository.countTotalBookings();
            stats.put("totalBookings", totalBookings != null ? totalBookings : 0L);
        } catch (Exception e) {
            stats.put("totalBookings", 0L);
            log.error("Error retrieving total bookings", e);
        }

        try {
            BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
            stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        } catch (Exception e) {
            stats.put("totalRevenue", BigDecimal.ZERO);
            log.error("Error retrieving total revenue", e);
        }

        try {
            YearMonth currentMonth = YearMonth.now();
            BigDecimal monthlyRevenue = bookingRepository.calculateMonthlyRevenue(
                    currentMonth.getYear(),
                    currentMonth.getMonthValue()
            );
            stats.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        } catch (Exception e) {
            stats.put("monthlyRevenue", BigDecimal.ZERO);
            log.error("Error retrieving monthly revenue", e);
        }

        log.info("Dashboard stats retrieved");
        return stats;
    }

    public Map<String, Object> getLast6MonthsRevenue() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth target = currentMonth.minusMonths(i);
            labels.add("Tháng " + target.getMonthValue() + " " + target.getYear());
            try {
                BigDecimal revenue = bookingRepository.calculateMonthlyRevenue(target.getYear(), target.getMonthValue());
                data.add(revenue != null ? revenue : BigDecimal.ZERO);
            } catch (Exception e) {
                log.error("Error retrieving revenue for month {}/{}", target.getMonthValue(), target.getYear(), e);
                data.add(BigDecimal.ZERO);
            }
        }

        result.put("labels", labels);
        result.put("data", data);
        return result;
    }

    public Map<String, Long> getBookingStatusBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            try {
                Long count = bookingRepository.countByStatus(status);
                breakdown.put(status.name(), count != null ? count : 0L);
            } catch (Exception e) {
                log.error("Error retrieving booking count for status {}", status, e);
                breakdown.put(status.name(), 0L);
            }
        }
        return breakdown;
    }

    /**
     * Lấy doanh thu theo khoảng thời gian
     */
    public BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            BigDecimal revenue = bookingRepository.calculateRevenueByDateRange(startDate, endDate);
            return revenue != null ? revenue : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating revenue by date range", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Lấy thống kê chi tiết
     */
    public Map<String, Object> getDetailedStats() {
        Map<String, Object> stats = getDashboardStats();


        Long activeCustomers = userRepository.countActiveCustomers();
        stats.put("activeCustomers", activeCustomers != null ? activeCustomers : 0L);

        return stats;
    }
}