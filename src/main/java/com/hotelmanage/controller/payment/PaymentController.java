package com.hotelmanage.controller.payment;


import com.hotelmanage.entity.User;
import com.hotelmanage.entity.booking.Booking;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import com.hotelmanage.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/booking/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @GetMapping("/{bookingId}")
    public String showPaymentPage(@PathVariable Integer bookingId,
                                  Principal principal,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingRepository.findById(bookingId.longValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking!"));

            // Kiểm tra quyền truy cập
            if (principal != null) {
                User currentUser = userRepository.findByUsername(principal.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (!booking.getUser().getId().equals(currentUser.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập booking này!");
                    return "redirect:/booking/search";
                }
            }

            model.addAttribute("currentStep", 3);
            model.addAttribute("booking", booking);
            model.addAttribute("bookingId", bookingId);

            return "booking/booking-form";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking/search";
        }
    }


    @PostMapping("/create")
    public String createPayment(@RequestParam Long bookingId,
                                HttpServletRequest request,
                                Model model) {
        try {
            String paymentUrl = paymentService.createPaymentUrl(bookingId, request);
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            log.error("Error creating payment: ", e);
            model.addAttribute("error", e.getMessage());
            return "booking/payment-error";
        }
    }

    @GetMapping("/callback")
    public String paymentCallback(@RequestParam Map<String, String> params, Model model) {
        boolean isSuccess = paymentService.handlePaymentCallback(params);

        if (isSuccess) {
            model.addAttribute("message", "Thanh toán thành công!");
            return "booking/payment-success";
        } else {
            model.addAttribute("error", "Thanh toán thất bại!");
            return "booking/payment-failed";
        }
    }
}
