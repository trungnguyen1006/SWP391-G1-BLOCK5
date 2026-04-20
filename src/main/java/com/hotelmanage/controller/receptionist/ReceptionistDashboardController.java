package com.hotelmanage.controller.receptionist;

import com.hotelmanage.entity.Enum.BookingStatus;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/reception")
@RequiredArgsConstructor
@Slf4j
public class ReceptionistDashboardController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @GetMapping("")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();

        model.addAttribute("totalBookings", bookingRepository.countTotalBookings());
        model.addAttribute("activeCustomers", userRepository.countActiveCustomers());
        model.addAttribute("pendingBookings", bookingRepository.countByStatus(BookingStatus.PENDING));
        model.addAttribute("checkedInBookings", bookingRepository.countByStatus(BookingStatus.CHECKED_IN));
        model.addAttribute("todayArrivals", bookingRepository.findByCheckInDateAndStatus(today, BookingStatus.CONFIRMED));
        model.addAttribute("todayDepartures", bookingRepository.findByCheckOutDateAndStatus(today, BookingStatus.CHECKED_IN));
        model.addAttribute("countPending", bookingRepository.countByStatus(BookingStatus.PENDING));
        model.addAttribute("countConfirmed", bookingRepository.countByStatus(BookingStatus.CONFIRMED));
        model.addAttribute("countCheckedIn", bookingRepository.countByStatus(BookingStatus.CHECKED_IN));
        model.addAttribute("countCheckedOut", bookingRepository.countByStatus(BookingStatus.CHECKED_OUT));
        model.addAttribute("countCancelled", bookingRepository.countByStatus(BookingStatus.CANCELLED_PERMANENTLY));
        model.addAttribute("today", today);

        log.debug("Rendered receptionist dashboard for date {}", today);
        return "receptionist/receptionist-dashboard";
    }
}

