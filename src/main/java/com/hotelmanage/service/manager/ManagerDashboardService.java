package com.hotelmanage.service.manager;

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
public class ManagerDashboardService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;
    private final FeedbackRepository feedbackRepository;
    private final BlogRepository blogRepository;
    private final RestaurantRepository restaurantRepository;
    private final PromotionRepository promotionRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();

        safeput(stats, "totalRooms",       () -> roomRepository.countTotalRooms());
        safeput(stats, "availableRooms",   () -> roomRepository.countAvailableRooms());
        safeput(stats, "monthlyBookings",  () -> bookingRepository.countMonthlyBookings(
                currentMonth.getYear(), currentMonth.getMonthValue()));
        safeput(stats, "monthlyRevenue",   () -> bookingRepository.calculateMonthlyRevenue(
                currentMonth.getYear(), currentMonth.getMonthValue()));
        safeput(stats, "totalRoomTypes",   () -> roomTypeRepository.countAllActive());
        safeput(stats, "totalBlogs",       () -> blogRepository.count());
        safeput(stats, "totalFeedbacks",   () -> feedbackRepository.count());
        safeput(stats, "totalRestaurants", () -> restaurantRepository.count());
        safeput(stats, "activePromotions", () -> promotionRepository.countActivePromotions(today));

        log.info("Manager dashboard stats loaded");
        return stats;
    }

    private void safeput(Map<String, Object> stats, String key, StatSupplier supplier) {
        try {
            stats.put(key, supplier.get());
        } catch (Exception e) {
            log.error("Error loading manager stat [{}]: {}", key, e.getMessage());
            stats.put(key, 0L);
        }
    }

    @FunctionalInterface
    private interface StatSupplier {
        Object get() throws Exception;
    }
}
