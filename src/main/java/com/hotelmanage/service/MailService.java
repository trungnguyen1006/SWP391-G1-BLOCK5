package com.hotelmanage.service;

import com.hotelmanage.entity.booking.Booking;
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

}


