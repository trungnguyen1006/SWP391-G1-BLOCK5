package com.hotelmanage.service;

import com.hotelmanage.entity.booking.Booking;
import com.hotelmanage.entity.restaurant.RestaurantBooking;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã OTP đặt lại mật khẩu");
        message.setText("Mã OTP của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút.");
        mailSender.send(message);
    }

    public void sendRegisterOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã OTP xác thực đăng ký");
        message.setText("Mã OTP xác thực đăng ký của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút.");
        mailSender.send(message);
    }

    public void sendBookingConfirmation(Booking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String emailContent = String.format("""
            Kính gửi Quý khách,
            
            Cảm ơn Quý khách đã đặt phòng tại khách sạn của chúng tôi!
            
            THÔNG TIN ĐẶT PHÒNG:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            Mã đặt phòng: %s
            Loại phòng: %s
            Số phòng: %s
            Ngày nhận phòng: %s (Trước 12:00 trưa)
            Ngày trả phòng: %s (Trước 14:00 chiều)
            Tổng tiền: %,.0f VNĐ
            Trạng thái: ĐÃ XÁC NHẬN
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            Lưu ý:
            - Vui lòng mang theo CMND/CCCD khi nhận phòng
            - Check-in: Trước 12:00 trưa
            - Check-out: Trước 14:00 chiều
            
            Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.
            
            Trân trọng,
            Khách sạn Hội An
            """,
                booking.getBookingId(),
                booking.getRoom().getRoomType().getRoomTypeName(),
                booking.getRoom().getRoomNumber(),
                booking.getCheckInDate().format(dateFormatter),
                booking.getCheckOutDate().format(dateFormatter),
                booking.getTotalPrice()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(booking.getUser().getEmail());
        message.setSubject("Xác nhận đặt phòng #" + booking.getBookingId() + " - Khách sạn Hội An");
        message.setText(emailContent);

        mailSender.send(message);
    }

    // ── Email 1: Gửi ngay khi khách đặt bàn (PENDING) ──────────────────
    public void sendRestaurantBookingReceived(RestaurantBooking booking) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String guestName = resolveGuestName(booking);

        String body = String.format("""
                Kính gửi %s,

                Chúng tôi đã nhận được yêu cầu đặt bàn của bạn tại %s.
                Nhà hàng sẽ xác nhận trong vòng 30 phút.

                THÔNG TIN ĐẶT BÀN:
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Mã đặt bàn  : #%d
                Nhà hàng    : %s
                Ngày        : %s
                Ca          : %s
                Số khách    : %d người
                Trạng thái  : CHỜ XÁC NHẬN
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                %s
                Liên hệ hỗ trợ: %s

                Trân trọng,
                Khách sạn Hội An
                """,
                guestName,
                booking.getRestaurant().getName(),
                booking.getBookingId(),
                booking.getRestaurant().getName(),
                booking.getBookingDate().format(fmt),
                booking.getBookingShift().getLabel(),
                booking.getNumberOfGuests(),
                formatSpecialRequest(booking),
                resolveContact(booking)
        );

        send(booking.getUser().getEmail(),
             "[Đặt bàn #" + booking.getBookingId() + "] Đã nhận – " + booking.getRestaurant().getName(),
             body);
    }

    // ── Email 2: Gửi khi nhân viên xác nhận (CONFIRMED) ────────────────
    public void sendRestaurantBookingConfirmed(RestaurantBooking booking) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String guestName = resolveGuestName(booking);

        String body = String.format("""
                Kính gửi %s,

                Đặt bàn của bạn đã được XÁC NHẬN! Hẹn gặp bạn tại %s.

                THÔNG TIN ĐẶT BÀN:
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Mã đặt bàn  : #%d
                Nhà hàng    : %s
                Ngày        : %s
                Ca          : %s
                Số khách    : %d người
                Trạng thái  : ĐÃ XÁC NHẬN
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                %s
                Lưu ý: Vui lòng có mặt đúng giờ. Bàn sẽ được giữ trong 15 phút.
                Liên hệ hỗ trợ: %s

                Trân trọng,
                Khách sạn Hội An
                """,
                guestName,
                booking.getRestaurant().getName(),
                booking.getBookingId(),
                booking.getRestaurant().getName(),
                booking.getBookingDate().format(fmt),
                booking.getBookingShift().getLabel(),
                booking.getNumberOfGuests(),
                formatSpecialRequest(booking),
                resolveContact(booking)
        );

        send(booking.getUser().getEmail(),
             "✅ [Đặt bàn #" + booking.getBookingId() + "] Đã xác nhận – " + booking.getRestaurant().getName(),
             body);
    }

    // ── Email 3: Gửi khi booking bị hủy (CANCELLED) ────────────────────
    public void sendRestaurantBookingCancelled(RestaurantBooking booking) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String guestName = resolveGuestName(booking);
        String reason = (booking.getCancelReason() != null && !booking.getCancelReason().isBlank())
                ? booking.getCancelReason()
                : "Không có lý do cụ thể";

        String body = String.format("""
                Kính gửi %s,

                Rất tiếc, đặt bàn của bạn tại %s đã bị HỦY.

                THÔNG TIN ĐẶT BÀN:
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Mã đặt bàn  : #%d
                Nhà hàng    : %s
                Ngày        : %s
                Ca          : %s
                Số khách    : %d người
                Trạng thái  : ĐÃ HỦY
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Lý do hủy   : %s

                Nếu bạn muốn đặt lại hoặc cần hỗ trợ, vui lòng liên hệ: %s

                Xin lỗi vì sự bất tiện này.
                Trân trọng,
                Khách sạn Hội An
                """,
                guestName,
                booking.getRestaurant().getName(),
                booking.getBookingId(),
                booking.getRestaurant().getName(),
                booking.getBookingDate().format(fmt),
                booking.getBookingShift().getLabel(),
                booking.getNumberOfGuests(),
                reason,
                resolveContact(booking)
        );

        send(booking.getUser().getEmail(),
             "❌ [Đặt bàn #" + booking.getBookingId() + "] Đã hủy – " + booking.getRestaurant().getName(),
             body);
    }

    // ── Helpers ─────────────────────────────────────────────────────────
    private void send(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }

    private String resolveGuestName(RestaurantBooking booking) {
        // GUEST user lưu tên vào trường address
        String addr = booking.getUser().getAddress();
        return (addr != null && !addr.isBlank()) ? addr : booking.getUser().getUsername();
    }

    private String resolveContact(RestaurantBooking booking) {
        String c = booking.getRestaurant().getContactInfo();
        return (c != null && !c.isBlank()) ? c : "lễ tân khách sạn";
    }

    private String formatSpecialRequest(RestaurantBooking booking) {
        String sr = booking.getSpecialRequest();
        return (sr != null && !sr.isBlank()) ? "Yêu cầu đặc biệt: " + sr + "\n" : "";
    }

}


