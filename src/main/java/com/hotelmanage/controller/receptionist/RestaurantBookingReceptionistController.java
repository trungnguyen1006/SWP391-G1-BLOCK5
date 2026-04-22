package com.hotelmanage.controller.receptionist;

import com.hotelmanage.entity.Enum.RestaurantBookingStatus;
import com.hotelmanage.service.restaurant.RestaurantBookingService;
import com.hotelmanage.repository.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reception/restaurant-bookings")
@RequiredArgsConstructor
@Slf4j
public class RestaurantBookingReceptionistController {

    private final RestaurantBookingService bookingService;
    private final RestaurantRepository     restaurantRepository;

    // ─────────────────────────────────────────────────────────────────
    // DANH SÁCH — có filter status + restaurant + phân trang
    // ─────────────────────────────────────────────────────────────────
    @GetMapping
    public String list(@RequestParam(required = false) RestaurantBookingStatus status,
                       @RequestParam(required = false) Long restaurantId,
                       @RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "15") int size,
                       Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        model.addAttribute("bookingPage",  bookingService.getBookings(status, restaurantId, pageable));
        model.addAttribute("restaurants",  restaurantRepository.findAllByOrderByNameAsc());
        model.addAttribute("statusList",   RestaurantBookingStatus.values());
        model.addAttribute("selectedStatus",     status);
        model.addAttribute("selectedRestaurant", restaurantId);

        return "receptionist/restaurant-booking/list";
    }

    // ─────────────────────────────────────────────────────────────────
    // XÁC NHẬN booking
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận đặt bàn #" + id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/restaurant-bookings";
    }

    // ─────────────────────────────────────────────────────────────────
    // HỦY booking
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @RequestParam(required = false) String cancelReason,
                         RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id, cancelReason);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đặt bàn #" + id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/restaurant-bookings";
    }
}
