package com.hotelmanage.service.restaurant;

import com.hotelmanage.entity.Enum.BookingShift;
import com.hotelmanage.entity.Enum.RestaurantBookingStatus;
import com.hotelmanage.entity.Enum.UserRole;
import com.hotelmanage.entity.Enum.UserStatus;
import com.hotelmanage.entity.User;
import com.hotelmanage.entity.restaurant.Restaurant;
import com.hotelmanage.entity.restaurant.RestaurantBooking;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.restaurant.RestaurantBookingRepository;
import com.hotelmanage.repository.restaurant.RestaurantRepository;
import com.hotelmanage.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RestaurantBookingService {

    private final RestaurantBookingRepository bookingRepository;
    private final RestaurantRepository        restaurantRepository;
    private final UserRepository              userRepository;
    private final MailService                 mailService;

    // ─────────────────────────────────────────────────────────────────
    // ĐẶT BÀN — user đã đăng nhập
    // ─────────────────────────────────────────────────────────────────
    public RestaurantBooking createBooking(String username,
                                           Long restaurantId,
                                           LocalDate bookingDate,
                                           BookingShift shift,
                                           Integer numberOfGuests,
                                           String specialRequest) {

        log.info("Creating restaurant booking for user={}, restaurant={}, date={}, shift={}",
                username, restaurantId, bookingDate, shift);

        // Validate số khách
        validateNumberOfGuests(numberOfGuests);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        Restaurant restaurant = getRestaurantAndValidate(restaurantId, bookingDate, shift);

        // Kiểm tra user đã đặt bàn cùng ca này chưa
        checkDuplicateBooking(user.getId(), restaurantId, bookingDate, shift);

        RestaurantBooking booking = RestaurantBooking.builder()
                .user(user)
                .restaurant(restaurant)
                .bookingDate(bookingDate)
                .bookingShift(shift)
                .numberOfGuests(numberOfGuests)
                .specialRequest(specialRequest)
                .build();

        RestaurantBooking saved = bookingRepository.save(booking);
        log.info("Restaurant booking created: id={}", saved.getBookingId());

        // Gửi email xác nhận (không throw nếu lỗi mail)
        try {
            mailService.sendRestaurantBookingReceived(saved);
            log.info("Confirmation email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send confirmation email for booking id={}: {}", saved.getBookingId(), e.getMessage());
        }

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────
    // ĐẶT BÀN — khách vãng lai (chưa đăng nhập)
    // ─────────────────────────────────────────────────────────────────
    public RestaurantBooking createGuestBooking(Long restaurantId,
                                                LocalDate bookingDate,
                                                BookingShift shift,
                                                Integer numberOfGuests,
                                                String specialRequest,
                                                String guestName,
                                                String guestPhone,
                                                String guestEmail) {

        log.info("Creating guest restaurant booking for email={}, restaurant={}, date={}, shift={}",
                guestEmail, restaurantId, bookingDate, shift);

        // Validate ngày không được trong quá khứ
        if (bookingDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày đặt bàn không được trong quá khứ!");
        }

        // Validate số khách
        validateNumberOfGuests(numberOfGuests);

        Restaurant restaurant = getRestaurantAndValidate(restaurantId, bookingDate, shift);

        User guestUser = findOrCreateGuestUser(guestEmail, guestPhone, guestName);

        // Kiểm tra email này đã đặt bàn cùng ca chưa
        checkDuplicateBooking(guestUser.getId(), restaurantId, bookingDate, shift);

        RestaurantBooking booking = RestaurantBooking.builder()
                .user(guestUser)
                .restaurant(restaurant)
                .bookingDate(bookingDate)
                .bookingShift(shift)
                .numberOfGuests(numberOfGuests)
                .specialRequest(specialRequest)
                .build();

        RestaurantBooking saved = bookingRepository.save(booking);
        log.info("Guest restaurant booking created: id={} for email={}", saved.getBookingId(), guestEmail);

        // Gửi email xác nhận (không throw nếu lỗi mail)
        try {
            mailService.sendRestaurantBookingReceived(saved);
            log.info("Confirmation email sent to guest {}", guestEmail);
        } catch (Exception e) {
            log.warn("Failed to send confirmation email for guest booking id={}: {}", saved.getBookingId(), e.getMessage());
        }

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────
    // XÁC NHẬN booking — Admin / Receptionist
    // ─────────────────────────────────────────────────────────────────
    public void confirmBooking(Long bookingId) {
        RestaurantBooking booking = findById(bookingId);

        if (booking.getStatus() == RestaurantBookingStatus.CANCELLED) {
            throw new RuntimeException("Không thể xác nhận booking đã hủy!");
        }
        if (booking.getStatus() == RestaurantBookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking đã được xác nhận trước đó!");
        }

        booking.setStatus(RestaurantBookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        log.info("Confirmed restaurant booking id={}", bookingId);

        // Gửi email thông báo đã xác nhận
        try {
            mailService.sendRestaurantBookingConfirmed(booking);
            log.info("Confirmed email sent to {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.warn("Failed to send confirmed email for booking id={}: {}", bookingId, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // HỦY booking — Receptionist / chính user (khi còn PENDING)
    // ─────────────────────────────────────────────────────────────────
    public void cancelBooking(Long bookingId, String reason) {
        RestaurantBooking booking = findById(bookingId);

        if (booking.getStatus() == RestaurantBookingStatus.CANCELLED) {
            throw new RuntimeException("Booking đã được hủy trước đó!");
        }

        booking.setStatus(RestaurantBookingStatus.CANCELLED);
        booking.setCancelReason(reason != null ? reason.trim() : null);
        bookingRepository.save(booking);
        log.info("Cancelled restaurant booking id={}, reason={}", bookingId, reason);

        try {
            mailService.sendRestaurantBookingCancelled(booking);
            log.info("Cancellation email sent to {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.warn("Failed to send cancellation email for booking id={}: {}", bookingId, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // HỦY của user — chỉ cho phép hủy khi còn PENDING
    // ─────────────────────────────────────────────────────────────────
    public void cancelByUser(Long bookingId, String username, String reason) {
        RestaurantBooking booking = findById(bookingId);

        // Chỉ owner mới được hủy
        if (!booking.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền hủy booking này!");
        }
        if (booking.getStatus() != RestaurantBookingStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy booking đang ở trạng thái Chờ xác nhận!");
        }

        booking.setStatus(RestaurantBookingStatus.CANCELLED);
        booking.setCancelReason(reason != null ? reason.trim() : null);
        bookingRepository.save(booking);
        log.info("User {} cancelled restaurant booking id={}", username, bookingId);

        try {
            mailService.sendRestaurantBookingCancelled(booking);
            log.info("Cancellation email sent to {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.warn("Failed to send cancellation email for booking id={}: {}", bookingId, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // QUERY
    // ─────────────────────────────────────────────────────────────────

    /** Lịch sử đặt bàn của user */
    @Transactional(readOnly = true)
    public List<RestaurantBooking> getMyBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    /** Admin/Receptionist: danh sách có filter + phân trang */
    @Transactional(readOnly = true)
    public Page<RestaurantBooking> getBookings(RestaurantBookingStatus status,
                                               Long restaurantId,
                                               Pageable pageable) {
        if (status != null && restaurantId != null) {
            return bookingRepository.findByStatusAndRestaurantIdOrderByCreatedAtDesc(
                    status, restaurantId, pageable);
        }
        if (status != null) {
            return bookingRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        if (restaurantId != null) {
            return bookingRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable);
        }
        return bookingRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /** Tìm theo ID */
    @Transactional(readOnly = true)
    public RestaurantBooking findById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking nhà hàng!"));
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER — Validate restaurant, ca và kiểm tra còn bàn không
    // ─────────────────────────────────────────────────────────────────
    private Restaurant getRestaurantAndValidate(Long restaurantId,
                                                LocalDate bookingDate,
                                                BookingShift shift) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng!"));

        // Kiểm tra nhà hàng có mở ca này không
        if (!isShiftAvailable(restaurant, shift)) {
            throw new RuntimeException(
                    "Nhà hàng không phục vụ " + shift.getDisplayName() + "!");
        }

        // Kiểm tra còn bàn trống không
        long activeCount = bookingRepository.countActiveBookings(
                restaurantId,
                bookingDate,
                shift,
                List.of(RestaurantBookingStatus.PENDING, RestaurantBookingStatus.CONFIRMED)
        );

        if (activeCount >= restaurant.getMaxTables()) {
            throw new RuntimeException(
                    shift.getDisplayName() + " ngày " + bookingDate
                    + " đã đầy chỗ. Vui lòng chọn ca hoặc ngày khác!");
        }

        return restaurant;
    }

    /** Kiểm tra nhà hàng có mở ca được chọn không */
    private boolean isShiftAvailable(Restaurant restaurant, BookingShift shift) {
        return switch (shift) {
            case SANG  -> Boolean.TRUE.equals(restaurant.getHasMorning());
            case TRUA  -> Boolean.TRUE.equals(restaurant.getHasLunch());
            case CHIEU -> Boolean.TRUE.equals(restaurant.getHasAfternoon());
            case TOI   -> Boolean.TRUE.equals(restaurant.getHasDinner());
        };
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER — Tạo / tìm GUEST user (tái sử dụng pattern BookingService)
    // ─────────────────────────────────────────────────────────────────
    private User findOrCreateGuestUser(String email, String phone, String name) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    // Email đã thuộc CUSTOMER/ADMIN/RECEPTIONIST → yêu cầu đăng nhập
                    if (existing.getRole() == UserRole.CUSTOMER
                            || existing.getRole() == UserRole.ADMIN
                            || existing.getRole() == UserRole.RECEPTIONIST) {
                        throw new RuntimeException(
                                "Email này đã được đăng ký. Vui lòng đăng nhập để đặt bàn!");
                    }
                    // GUEST cũ → cập nhật thông tin
                    existing.setPhone(phone);
                    existing.setAddress(name);
                    log.info("Updated existing guest user for restaurant booking: {}", email);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User guest = User.builder()
                            .username("guest_" + email.split("@")[0]
                                      + "_" + System.currentTimeMillis())
                            .password("")
                            .email(email)
                            .phone(phone)
                            .address(name)   // dùng address lưu tên khách
                            .role(UserRole.GUEST)
                            .status(UserStatus.INACTIVE)
                            .build();
                    User saved = userRepository.save(guest);
                    log.info("Created guest user for restaurant booking: {}", email);
                    return saved;
                });
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER — Validate số lượng khách (1–20 người / booking)
    // ─────────────────────────────────────────────────────────────────
    private void validateNumberOfGuests(Integer numberOfGuests) {
        if (numberOfGuests == null || numberOfGuests < 1) {
            throw new RuntimeException("Số khách phải ít nhất là 1 người!");
        }
        if (numberOfGuests > 20) {
            throw new RuntimeException(
                    "Mỗi lần đặt tối đa 20 khách. Nhóm lớn hơn vui lòng liên hệ trực tiếp nhà hàng!");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER — Chặn đặt trùng: 1 user chỉ được 1 booking / ca / nhà hàng
    // ─────────────────────────────────────────────────────────────────
    private void checkDuplicateBooking(Long userId, Long restaurantId,
                                       LocalDate bookingDate, BookingShift shift) {
        long existing = bookingRepository.countUserActiveBookings(
                userId, restaurantId, bookingDate, shift,
                List.of(RestaurantBookingStatus.PENDING, RestaurantBookingStatus.CONFIRMED)
        );
        if (existing > 0) {
            throw new RuntimeException(
                    "Bạn đã có đặt bàn cho ca " + shift.getDisplayName()
                    + " ngày " + bookingDate + " tại nhà hàng này rồi!");
        }
    }
}
