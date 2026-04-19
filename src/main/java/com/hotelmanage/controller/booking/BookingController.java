package com.hotelmanage.controller.booking;


import com.hotelmanage.entity.User;
import com.hotelmanage.entity.booking.Booking;
import com.hotelmanage.entity.booking.Promotion;
import com.hotelmanage.entity.room.Room;
import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import com.hotelmanage.service.booking.BookingService;
import com.hotelmanage.service.booking.PromotionService;
import com.hotelmanage.service.payment.PaymentService;
import com.hotelmanage.service.room.RoomService;
import com.hotelmanage.service.room.RoomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final RoomTypeService roomTypeService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final PromotionService promotionService;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;

    @GetMapping("/search")
    public String searchRooms(@RequestParam(required = false) LocalDate checkInDate,
                              @RequestParam(required = false) LocalDate checkOutDate,
                              @RequestParam(required = false) Integer amountPerson,
                              @RequestParam(required = false) String promotionCode,
                              Model model) {

        if (checkInDate == null) {
            checkInDate = LocalDate.now();
        }
        if (checkOutDate == null) {
            checkOutDate = LocalDate.now().plusDays(1);
        }
        if (amountPerson == null) {
            amountPerson = 1;
        }

        model.addAttribute("currentStep", 1);


        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            model.addAttribute("error", "Ngày trả phòng phải sau ngày nhận phòng!");
            model.addAttribute("checkInDate", checkInDate);
            model.addAttribute("checkOutDate", checkOutDate);
            model.addAttribute("amountPerson", amountPerson);
            model.addAttribute("promotionCode", promotionCode);
            return "booking/booking-form";
        }

        List<RoomType> roomTypes = roomTypeService.searchAvailableRoomTypes(
                checkInDate, checkOutDate, amountPerson);

        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("checkInDate", checkInDate);
        model.addAttribute("checkOutDate", checkOutDate);
        model.addAttribute("amountPerson", amountPerson);
        model.addAttribute("promotionCode", promotionCode);

        if (roomTypes == null || roomTypes.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy phòng phù hợp với yêu cầu của bạn.");
        }

        return "booking/booking-form";
    }

    @GetMapping("/room-detail/{roomTypeId}")
    public String roomDetail(@PathVariable Integer roomTypeId,
                             @RequestParam LocalDate checkInDate,
                             @RequestParam LocalDate checkOutDate,
                             @RequestParam Integer amountPerson,
                             @RequestParam(required = false) String promotionCode,
                             Principal principal,
                             Model model) {

        RoomType roomType = roomTypeService.findById(roomTypeId);

        // Thêm thông tin user nếu đã đăng nhập
        if (principal != null) {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElse(null);
            model.addAttribute("currentUser", currentUser);
        }

        model.addAttribute("roomType", roomType);
        model.addAttribute("checkInDate", checkInDate);
        model.addAttribute("checkOutDate", checkOutDate);
        model.addAttribute("amountPerson", amountPerson);
        model.addAttribute("promotionCode", promotionCode);

        return "booking/room-detail";
    }


    @PostMapping("/select-room")
    public String selectRoom(@RequestParam Integer roomTypeId,
                             @RequestParam LocalDate checkInDate,
                             @RequestParam LocalDate checkOutDate,
                             @RequestParam Integer amountPerson,
                             @RequestParam(required = false) String promotionCode,
                             Principal principal,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        try {
            RoomType selectedRoomType = roomTypeService.findById(roomTypeId);

            // TÌM PHÒNG AVAILABLE NGAY TẠI ĐÂY
            Room availableRoom = roomService.findAvailableRoom(roomTypeId, checkInDate, checkOutDate);

            long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            BigDecimal pricePerNight = selectedRoomType.getPrice();
            BigDecimal totalAmount = pricePerNight.multiply(BigDecimal.valueOf(numberOfNights));
            BigDecimal discount = BigDecimal.ZERO;
            Promotion promotion = null;

            if (promotionCode != null && !promotionCode.isEmpty()) {
                try {
                    promotion = promotionService.validatePromotion(promotionCode);
                    discount = promotion.getDiscountAmount();
                    totalAmount = totalAmount.subtract(discount);
                } catch (RuntimeException e) {
                    model.addAttribute("promotionError", e.getMessage());
                }
            }

            // Thêm thông tin user nếu đã đăng nhập
            if (principal != null) {
                User currentUser = userRepository.findByUsername(principal.getName())
                        .orElse(null);
                model.addAttribute("currentUser", currentUser);
            }

            model.addAttribute("currentStep", 2);
            model.addAttribute("selectedRoomType", selectedRoomType);
            model.addAttribute("selectedRoom", availableRoom); // THÊM ROOM CỤ THỂ
            model.addAttribute("checkInDate", checkInDate);
            model.addAttribute("checkOutDate", checkOutDate);
            model.addAttribute("amountPerson", amountPerson);
            model.addAttribute("promotionCode", promotionCode);
            model.addAttribute("promotion", promotion);
            model.addAttribute("numberOfNights", numberOfNights);
            model.addAttribute("discount", discount);
            model.addAttribute("totalAmount", totalAmount);

            return "booking/booking-form";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return redirectToSelectRoom(roomTypeId, checkInDate, checkOutDate, amountPerson, promotionCode, redirectAttributes);
        }
    }

    @PostMapping("/confirm")
    public String confirmBooking(@RequestParam Integer roomTypeId,
                                 @RequestParam LocalDate checkInDate,
                                 @RequestParam LocalDate checkOutDate,
                                 @RequestParam Integer amountPerson,
                                 @RequestParam(required = false) String promotionCode,
                                 @RequestParam(required = false) String customerName,
                                 @RequestParam(required = false) String customerPhone,
                                 @RequestParam(required = false) String customerEmail,
                                 @RequestParam(required = false) String customerAddress,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {

        try {
            // Validate thông tin khách hàng bắt buộc (khi không đăng nhập)
            if (principal == null) {
                if (customerName == null || customerName.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng nhập tên khách hàng!");
                    return redirectToSelectRoom(roomTypeId, checkInDate, checkOutDate, amountPerson, promotionCode, redirectAttributes);
                }
                if (customerPhone == null || customerPhone.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng nhập số điện thoại!");
                    return redirectToSelectRoom(roomTypeId, checkInDate, checkOutDate, amountPerson, promotionCode, redirectAttributes);
                }
                if (customerEmail == null || customerEmail.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng nhập email!");
                    return redirectToSelectRoom(roomTypeId, checkInDate, checkOutDate, amountPerson, promotionCode, redirectAttributes);
                }
            }

            // TÌM PHÒNG AVAILABLE - SẼ THROW EXCEPTION NẾU KHÔNG CÒN PHÒNG
            Room availableRoom = roomService.findAvailableRoom(roomTypeId, checkInDate, checkOutDate);

            log.info("Found available room: {} for roomType: {}", availableRoom.getRoomId(), roomTypeId);

            // Tính toán giá
            RoomType roomType = roomTypeService.findById(roomTypeId);
            long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            BigDecimal totalAmount = roomType.getPrice().multiply(BigDecimal.valueOf(numberOfNights));

            // Áp dụng promotion nếu có
            Integer promotionId = null;
            if (promotionCode != null && !promotionCode.isEmpty()) {
                try {
                    Promotion promotion = promotionService.validatePromotion(promotionCode);
                    totalAmount = totalAmount.subtract(promotion.getDiscountAmount());
                    promotionId = promotion.getPromotionId();
                    log.info("Applied promotion: {}, discount: {}", promotionCode, promotion.getDiscountAmount());
                } catch (RuntimeException e) {
                    log.warn("Invalid promotion code: {}", promotionCode);
                }
            }

            // Tạo booking với ROOM ID thực tế
            Booking booking;
            if (principal != null) {
                booking = bookingService.createBooking(
                        principal.getName(),
                        availableRoom.getRoomId(), // TRUYỀN ROOM ID
                        checkInDate,
                        checkOutDate,
                        totalAmount,
                        promotionId
                );
                log.info("Created booking for logged-in user: {}", principal.getName());
            } else {
                booking = bookingService.createGuestBooking(
                        availableRoom.getRoomId(), // TRUYỀN ROOM ID
                        checkInDate,
                        checkOutDate,
                        totalAmount,
                        promotionId,
                        customerPhone,
                        customerEmail,
                        customerAddress
                );
                log.info("Created guest booking for: {}", customerName);
            }

            redirectAttributes.addFlashAttribute("success",
                    "Đặt phòng thành công! Mã booking: " + booking.getBookingId());
            return "redirect:/booking/payment/" + booking.getBookingId();

        } catch (RuntimeException e) {
            log.error("Error creating booking: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return redirectToSelectRoom(roomTypeId, checkInDate, checkOutDate, amountPerson, promotionCode, redirectAttributes);
        }
    }


    /**
     * Helper method để redirect về select-room với params
     */
    private String redirectToSelectRoom(Integer roomTypeId, LocalDate checkInDate,
                                        LocalDate checkOutDate, Integer amountPerson,
                                        String promotionCode, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("roomTypeId", roomTypeId);
        redirectAttributes.addFlashAttribute("checkInDate", checkInDate);
        redirectAttributes.addFlashAttribute("checkOutDate", checkOutDate);
        redirectAttributes.addFlashAttribute("amountPerson", amountPerson);
        redirectAttributes.addFlashAttribute("promotionCode", promotionCode);
        return "redirect:/booking/search";
    }

    @GetMapping("/history")
    public String viewBookingHistory(Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("bookings", Collections.emptyList());
            return "booking/view-booking-history";
        }

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserId(currentUser.getId());

        model.addAttribute("bookings", bookings);
        model.addAttribute("currentUser", currentUser);

        return "booking/view-booking-history";
    }


}
