package com.hotelmanage.controller.admin.restaurant;

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
@RequestMapping("/admin/restaurant-bookings")
@RequiredArgsConstructor
@Slf4j
public class AdminRestaurantBookingController {

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

        return "admin/restaurant-booking/list";
    }

    // Restaurant booking confirmation and cancellation are restricted to receptionists only.
    // Admins can view bookings but cannot confirm or cancel them.
}
