package com.hotelmanage.service.dashboard;

import com.hotelmanage.entity.Enum.BookingStatus;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import com.hotelmanage.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    /**
     * Thống kê tổng quan — mỗi metric có try-catch riêng
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            Long totalRooms = roomRepository.countTotalRooms();
            stats.put("totalRooms", totalRooms != null ? totalRooms : 0L);
        } catch (Exception e) {
            log.error("Error counting total rooms", e);
            stats.put("totalRooms", 0L);
        }

        try {
            Long availableRooms = roomRepository.countAvailableRooms();
            stats.put("availableRooms", availableRooms != null ? availableRooms : 0L);
        } catch (Exception e) {
            log.error("Error counting available rooms", e);
            stats.put("availableRooms", 0L);
        }

        try {
            Long activeUsers = userRepository.countActiveUsers();
            stats.put("activeUsers", activeUsers != null ? activeUsers : 0L);
        } catch (Exception e) {
            log.error("Error counting active users", e);
            stats.put("activeUsers", 0L);
        }

        try {
            Long totalBookings = bookingRepository.countTotalBookings();
            stats.put("totalBookings", totalBookings != null ? totalBookings : 0L);
        } catch (Exception e) {
            log.error("Error counting total bookings", e);
            stats.put("totalBookings", 0L);
        }

        try {
            BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
            stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        } catch (Exception e) {
            log.error("Error calculating total revenue", e);
            stats.put("totalRevenue", BigDecimal.ZERO);
        }

        try {
            YearMonth currentMonth = YearMonth.now();
            BigDecimal monthlyRevenue = bookingRepository.calculateMonthlyRevenue(
                    currentMonth.getYear(), currentMonth.getMonthValue());
            stats.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        } catch (Exception e) {
            log.error("Error calculating monthly revenue", e);
            stats.put("monthlyRevenue", BigDecimal.ZERO);
        }

        log.info("Dashboard stats retrieved");
        return stats;
    }

    public BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            BigDecimal revenue = bookingRepository.calculateRevenueByDateRange(startDate, endDate);
            return revenue != null ? revenue : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating revenue by date range", e);
            return BigDecimal.ZERO;
        }
    }

    public Map<String, Object> getDetailedStats() {
        Map<String, Object> stats = getDashboardStats();
        try {
            Long activeCustomers = userRepository.countActiveCustomers();
            stats.put("activeCustomers", activeCustomers != null ? activeCustomers : 0L);
        } catch (Exception e) {
            log.error("Error counting active customers", e);
            stats.put("activeCustomers", 0L);
        }
        return stats;
    }

    /**
     * Doanh thu 6 tháng gần nhất — dữ liệu cho bar chart
     */
    public Map<String, Object> getLast6MonthsRevenue() {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            String label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("vi"))
                    + " " + month.getYear();
            BigDecimal revenue;
            try {
                revenue = bookingRepository.calculateMonthlyRevenue(month.getYear(), month.getMonthValue());
            } catch (Exception e) {
                log.error("Error getting revenue for {}", month, e);
                revenue = BigDecimal.ZERO;
            }
            labels.add(label);
            data.add(revenue != null ? revenue : BigDecimal.ZERO);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }

    /**
     * Số booking theo từng trạng thái — dữ liệu cho doughnut chart
     */
    public Map<String, Long> getBookingStatusBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            try {
                Long count = bookingRepository.countByStatus(status);
                breakdown.put(status.name(), count != null ? count : 0L);
            } catch (Exception e) {
                log.error("Error counting bookings for status {}", status, e);
                breakdown.put(status.name(), 0L);
            }
        }
        return breakdown;
    }
}
