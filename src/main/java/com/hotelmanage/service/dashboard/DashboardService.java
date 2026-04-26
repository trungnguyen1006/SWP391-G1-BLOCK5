package com.hotelmanage.service.dashboard;

import com.hotelmanage.entity.Enum.BookingStatus;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    /**
     * Thống kê tổng quan — mỗi metric có try-catch riêng
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            Long totalUsers = userRepository.countManageableUsers();
            stats.put("totalUsers", totalUsers != null ? totalUsers : 0L);
        } catch (Exception e) {
            log.error("Error counting managed users", e);
            stats.put("totalUsers", 0L);
        }

        try {
            Long activeUsers = userRepository.countActiveManageableUsers();
            stats.put("activeUsers", activeUsers != null ? activeUsers : 0L);
        } catch (Exception e) {
            log.error("Error counting active managed users", e);
            stats.put("activeUsers", 0L);
        }

        try {
            Long inactiveUsers = userRepository.countInactiveManageableUsers();
            stats.put("inactiveUsers", inactiveUsers != null ? inactiveUsers : 0L);
        } catch (Exception e) {
            log.error("Error counting inactive managed users", e);
            stats.put("inactiveUsers", 0L);
        }

        log.info("Dashboard stats retrieved");
        return stats;
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
