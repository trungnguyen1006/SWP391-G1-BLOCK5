package com.hotelmanage.service.dashboard;

import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.blog.BlogRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import com.hotelmanage.repository.booking.PromotionRepository;
import com.hotelmanage.repository.feedback.FeedbackRepository;
import com.hotelmanage.repository.restaurant.RestaurantRepository;
import com.hotelmanage.repository.room.RoomRepository;
import com.hotelmanage.repository.room.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final FeedbackRepository feedbackRepository;
    private final BlogRepository blogRepository;
    private final RestaurantRepository restaurantRepository;
    private final PromotionRepository promotionRepository;

    /**
     * Thống kê tổng quan cho Dashboard — trả về Map để đổ vào Thymeleaf model
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();

        // 1. Tổng số phòng
        safeput(stats, "totalRooms", () -> roomRepository.countTotalRooms());

        // 2. Phòng trống
        safeput(stats, "availableRooms", () -> roomRepository.countAvailableRooms());

        // 3. Đặt phòng tháng này
        safeput(stats, "monthlyBookings", () ->
                bookingRepository.countMonthlyBookings(currentMonth.getYear(), currentMonth.getMonthValue()));

        // 4. Doanh thu tháng này
        safeput(stats, "monthlyRevenue", () ->
                bookingRepository.calculateMonthlyRevenue(currentMonth.getYear(), currentMonth.getMonthValue()));

        // 5. Tổng số loại phòng
        safeput(stats, "totalRoomTypes", () -> roomTypeRepository.countAllActive());

        // 6. Tổng số blog
        safeput(stats, "totalBlogs", () -> blogRepository.count());

        // 7. Tổng số feedback
        safeput(stats, "totalFeedbacks", () -> feedbackRepository.count());

        // 8. Tổng số nhà hàng
        safeput(stats, "totalRestaurants", () -> restaurantRepository.count());

        // 9. Promotion đang hoạt động
        safeput(stats, "activePromotions", () -> promotionRepository.countActivePromotions(today));

        log.info("Dashboard stats loaded successfully");
        return stats;
    }

    private void safeput(Map<String, Object> stats, String key, StatSupplier supplier) {
        try {
            stats.put(key, supplier.get());
        } catch (Exception e) {
            log.error("Error loading stat [{}]: {}", key, e.getMessage());
            stats.put(key, 0L);
        }
    }

    @FunctionalInterface
    private interface StatSupplier {
        Object get() throws Exception;
    }
}
