package com.hotelmanage.service.dashboard;

import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.blog.BlogRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BlogRepository blogRepository;

    /**
     * Thống kê tổng quan cho Admin Dashboard
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

        log.info("Admin dashboard stats loaded");
        return stats;
    }

    public Map<String, Object> getDetailedStats() {
        return getDashboardStats();
    }
}
