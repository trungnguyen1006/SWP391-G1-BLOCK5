package com.hotelmanage.controller.receptionist;


import com.hotelmanage.entity.booking.Booking;
import com.hotelmanage.repository.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reception")
@RequiredArgsConstructor
public class BookingReceptionistController {

    private final BookingRepository bookingRepository;

    @GetMapping("/bookings")
    public String listAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String bookingId,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPage;

        // Tìm kiếm theo bookingId nếu có, ngược lại lấy tất cả
        if (bookingId != null && !bookingId.trim().isEmpty()) {
            bookingPage = bookingRepository.findBookingsByBookingIdContaining(bookingId, pageable);
        } else {
            bookingPage = bookingRepository.findAllBookingsWithPagination(pageable);
        }

        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", bookingPage.getNumber());
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        model.addAttribute("totalItems", bookingPage.getTotalElements());
        model.addAttribute("bookingId", bookingId); // Giữ lại giá trị search

        return "receptionist/booking/list";
    }


}

