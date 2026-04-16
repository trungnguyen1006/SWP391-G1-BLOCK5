package com.hotelmanage.service.booking;

import com.hotelmanage.entity.Enum.BookingStatus;
import com.hotelmanage.entity.Enum.UserRole;
import com.hotelmanage.entity.Enum.UserStatus;
import com.hotelmanage.entity.User;
import com.hotelmanage.entity.booking.Booking;
import com.hotelmanage.entity.booking.Promotion;
import com.hotelmanage.entity.room.Room;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import com.hotelmanage.repository.booking.PromotionRepository;
import com.hotelmanage.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PromotionService promotionService;
    private final PromotionRepository promotionRepository;

    public Booking createBooking(String username,
                                 Integer roomId, // Đổi tên parameter
                                 LocalDate checkInDate,
                                 LocalDate checkOutDate,
                                 BigDecimal totalPrice,
                                 Integer promotionId) {

        log.info("Creating booking for user: {}, roomId: {}", username, roomId);


        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // Tìm phòng theo roomId thay vì tìm available rooms
        Room selectedRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(selectedRoom);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        if (promotionId != null) {
            Promotion promotion = promotionService.findById(promotionId);
            booking.setPromotion(promotion);
            promotionService.incrementUsedCount(promotionId);
        }

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully: {}", savedBooking.getBookingId());
        return savedBooking;
    }

    public Booking createGuestBooking(Integer roomId, // Đổi tên parameter
                                      LocalDate checkInDate,
                                      LocalDate checkOutDate,
                                      BigDecimal totalPrice,
                                      Integer promotionId,
                                      String phone,
                                      String customerEmail,
                                      String address) {

        log.info("Creating guest booking for email: {}, roomId: {}", customerEmail, roomId);


        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new RuntimeException("Ngày trả phòng phải sau ngày nhận phòng!");
        }

        User guestUser = createGuestUser(customerEmail, phone, address);

        // Tìm phòng theo roomId thay vì tìm available rooms
        Room selectedRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        Booking booking = new Booking();
        booking.setUser(guestUser);
        booking.setRoom(selectedRoom);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        if (promotionId != null) {
            Promotion promotion = promotionRepository.findById(promotionId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion!"));
            booking.setPromotion(promotion);
            promotionService.incrementUsedCount(promotionId);
        }

        Booking saved = bookingRepository.save(booking);
        log.info("Created guest booking with ID: {} for email: {}", saved.getBookingId(), customerEmail);

        return saved;
    }

    /**
     * Tạo user guest tạm thời chỉ với email
     * User này có thể được nâng cấp lên CUSTOMER khi đăng ký tài khoản
     */
    private User createGuestUser(String customerEmail, String phone, String address) {
        // Kiểm tra email đã tồn tại
        return userRepository.findByEmail(customerEmail)
                .map(existingUser -> {
                    // Nếu email đã tồn tại với role CUSTOMER hoặc ADMIN
                    if (existingUser.getRole() == UserRole.CUSTOMER ||
                            existingUser.getRole() == UserRole.ADMIN || existingUser.getRole() == UserRole.RECEPTIONIST) {
                        throw new RuntimeException("Email này đã được đăng ký. Vui lòng đăng nhập để đặt phòng!");
                    }
                    // Nếu là GUEST cũ, cập nhật thông tin
                    existingUser.setPhone(phone);
                    existingUser.setAddress(address);
                    log.info("Updated existing guest user with email: {}", customerEmail);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // Tạo user guest mới
                    User guest = User.builder()
                            .username("guest_" + customerEmail.split("@")[0] + "_" + System.currentTimeMillis())
                            .password("")
                            .email(customerEmail)
                            .role(UserRole.GUEST)
                            .status(UserStatus.INACTIVE)
                            .phone(phone)
                            .address(address)
                            .build();

                    User saved = userRepository.save(guest);
                    log.info("Created temporary guest user with email: {}", customerEmail);
                    return saved;
                });
    }


}

