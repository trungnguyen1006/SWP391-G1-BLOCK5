package com.hotelmanage.controller.admin;

import com.hotelmanage.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Lấy thống kê tổng quan cho dashboard
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Getting dashboard statistics");
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Lấy thống kê chi tiết
     */
    @GetMapping("/stats/detailed")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getDetailedStats() {
        log.info("Getting detailed statistics");
        Map<String, Object> stats = dashboardService.getDetailedStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Dữ liệu doanh thu 6 tháng gần nhất cho bar chart
     */
    @GetMapping("/chart/monthly-revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenueChart() {
        log.info("Getting monthly revenue chart data");
        return ResponseEntity.ok(dashboardService.getLast6MonthsRevenue());
    }

    /**
     * Số booking theo trạng thái cho doughnut chart
     */
    @GetMapping("/chart/booking-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Long>> getBookingStatusChart() {
        log.info("Getting booking status breakdown");
        return ResponseEntity.ok(dashboardService.getBookingStatusBreakdown());
    }
}
