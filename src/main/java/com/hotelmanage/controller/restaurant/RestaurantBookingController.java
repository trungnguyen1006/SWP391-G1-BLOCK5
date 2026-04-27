package com.hotelmanage.controller.restaurant;

import com.hotelmanage.entity.Enum.BookingShift;
import com.hotelmanage.entity.restaurant.Restaurant;
import com.hotelmanage.entity.restaurant.RestaurantBooking;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.restaurant.RestaurantRepository;
import com.hotelmanage.service.restaurant.RestaurantBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RestaurantBookingController {

    private final RestaurantBookingService bookingService;
    private final RestaurantRepository     restaurantRepository;
    private final UserRepository           userRepository;

    // ─────────────────────────────────────────────────────────────────
    // FORM ĐẶT BÀN
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/restaurants/{id}/booking")
    public String showBookingForm(@PathVariable Long id, Model model, Principal principal) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng!"));

        List<BookingShift> availableShifts = getAvailableShifts(restaurant);

        if (availableShifts.isEmpty()) {
            model.addAttribute("error", "Nhà hàng hiện chưa mở ca đặt bàn nào.");
        }

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("availableShifts", availableShifts);
        model.addAttribute("minDate", LocalDate.now().plusDays(1).toString());

        if (principal != null) {
            userRepository.findByUsername(principal.getName())
                    .ifPresent(u -> model.addAttribute("currentUser", u));
        }

        return "restaurant/booking-form";
    }

    // ─────────────────────────────────────────────────────────────────
    // XỬ LÝ ĐẶT BÀN (user đã đăng nhập + khách vãng lai)
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/restaurants/{id}/booking")
    public String submitBooking(@PathVariable Long id,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                                @RequestParam BookingShift shift,
                                @RequestParam Integer numberOfGuests,
                                @RequestParam(required = false) String specialRequest,
                                // Trường cho khách vãng lai
                                @RequestParam(required = false) String guestName,
                                @RequestParam(required = false) String guestPhone,
                                @RequestParam(required = false) String guestEmail,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        try {
            RestaurantBooking booking;

            if (principal != null) {
                // Người dùng đã đăng nhập
                booking = bookingService.createBooking(
                        principal.getName(), id, bookingDate, shift, numberOfGuests, specialRequest);
            } else {
                // Khách vãng lai — validate input
                if (guestName == null || guestName.isBlank()) {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng nhập họ tên!");
                    return "redirect:/restaurants/" + id + "/booking";
                }
                if (guestPhone == null || guestPhone.isBlank()) {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng nhập số điện thoại!");
                    return "redirect:/restaurants/" + id + "/booking";
                }
                // Validate định dạng số điện thoại (Việt Nam: 10 chữ số, bắt đầu với 0)
                if (!guestPhone.trim().matches("^0\\d{9}$")) {
                    redirectAttributes.addFlashAttribute("error", "Số điện thoại không hợp lệ (phải là 10 chữ số, bắt đầu với 0)!");
                    return "redirect:/restaurants/" + id + "/booking";
                }
                if (guestEmail == null || guestEmail.isBlank()) {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng nhập email!");
                    return "redirect:/restaurants/" + id + "/booking";
                }
                // Validate định dạng email
                if (!guestEmail.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    redirectAttributes.addFlashAttribute("error", "Email không hợp lệ!");
                    return "redirect:/restaurants/" + id + "/booking";
                }

                booking = bookingService.createGuestBooking(
                        id, bookingDate, shift, numberOfGuests, specialRequest,
                        guestName.trim(), guestPhone.trim(), guestEmail.trim());
            }

            return "redirect:/restaurants/booking/success/" + booking.getBookingId();

        } catch (RuntimeException e) {
            log.warn("Restaurant booking failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/restaurants/" + id + "/booking";
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // TRANG THÀNH CÔNG
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/restaurants/booking/success/{bookingId}")
    public String bookingSuccess(@PathVariable Long bookingId, Model model) {
        RestaurantBooking booking = bookingService.findById(bookingId);
        model.addAttribute("booking", booking);
        return "restaurant/booking-success";
    }

    // ─────────────────────────────────────────────────────────────────
    // LỊCH SỬ ĐẶT BÀN CỦA TÔI
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/restaurants/my-bookings")
    public String myBookings(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        List<RestaurantBooking> bookings = bookingService.getMyBookings(principal.getName());
        model.addAttribute("bookings", bookings);
        return "restaurant/my-bookings";
    }

    // ─────────────────────────────────────────────────────────────────
    // HỦY BOOKING — do chính user thực hiện
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/restaurants/booking/{bookingId}/cancel")
    public String cancelMyBooking(@PathVariable Long bookingId,
                                  @RequestParam(required = false) String cancelReason,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            bookingService.cancelByUser(bookingId, principal.getName(), cancelReason);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đặt bàn thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/restaurants/my-bookings";
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────────────
    private List<BookingShift> getAvailableShifts(Restaurant r) {
        List<BookingShift> shifts = new ArrayList<>();
        if (Boolean.TRUE.equals(r.getHasMorning()))   shifts.add(BookingShift.SANG);
        if (Boolean.TRUE.equals(r.getHasLunch()))     shifts.add(BookingShift.TRUA);
        if (Boolean.TRUE.equals(r.getHasAfternoon())) shifts.add(BookingShift.CHIEU);
        if (Boolean.TRUE.equals(r.getHasDinner()))    shifts.add(BookingShift.TOI);
        return shifts;
    }
}
