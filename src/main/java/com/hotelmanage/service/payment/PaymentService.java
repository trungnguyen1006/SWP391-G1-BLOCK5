package com.hotelmanage.service.payment;


import com.hotelmanage.config.VNPayConfig;
import com.hotelmanage.entity.Enum.BookingStatus;
import com.hotelmanage.entity.Enum.PaymentStatus;
import com.hotelmanage.entity.Enum.RoomStatus;
import com.hotelmanage.entity.booking.Booking;
import com.hotelmanage.entity.payment.Payment;
import com.hotelmanage.entity.room.Room;
import com.hotelmanage.repository.booking.BookingRepository;
import com.hotelmanage.repository.payment.PaymentRepository;
import com.hotelmanage.repository.room.RoomRepository;
import com.hotelmanage.service.MailService;
import com.hotelmanage.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final VNPayConfig vnPayConfig;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;
    private final MailService mailService;

    public String createPaymentUrl(Long bookingId, HttpServletRequest request)
            throws UnsupportedEncodingException {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (booking.getStatus() == BookingStatus.CANCELLED_PERMANENTLY) {
            throw new RuntimeException("Booking đã bị hủy vĩnh viễn");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking đã được thanh toán");
        }

        Payment payment = paymentRepository.findLatestByBooking(booking)
                .orElse(null);

        // Nếu đã có payment FAILED -> không cho thanh toán lại
        if (payment != null && payment.getPaymentStatus() == PaymentStatus.FAILED) {
            booking.setStatus(BookingStatus.CANCELLED_PERMANENTLY);
            bookingRepository.save(booking);
            throw new RuntimeException("Thanh toán đã thất bại. Vui lòng đặt phòng lại.");
        }

        // Nếu có payment PENDING đã hết hạn -> set FAILED và hủy booking
        if (payment != null && payment.getPaymentStatus() == PaymentStatus.PENDING) {
            LocalDateTime expireTime = payment.getExpireTime();
            boolean isExpired = expireTime != null && LocalDateTime.now().isAfter(expireTime);

            if (isExpired) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                booking.setStatus(BookingStatus.CANCELLED_PERMANENTLY);
                bookingRepository.save(booking);

                throw new RuntimeException("Thanh toán đã hết hạn. Vui lòng đặt phòng lại.");
            }

            // Payment vẫn còn hạn -> giữ nguyên
            payment.setPaymentDate(LocalDateTime.now());
            payment = paymentRepository.save(payment);
        }

        // Tạo payment mới nếu chưa có
        if (payment == null) {
            payment = createNewPayment(booking);
        }

        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        // Bypass VNPay for demo
        // String vnp_TxnRef = booking.getBookingId() + "_" + payment.getPaymentId();
        // return "/booking/payment/callback?vnp_ResponseCode=00&vnp_TxnRef=" + vnp_TxnRef;
        return buildVNPayUrl(booking, payment, request);
    }

    /**
     * Xử lý callback từ VNPay
     */
    @Transactional
    public boolean handlePaymentCallback(Map<String, String> params) {
        try {
            String transactionId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String bookingIdStr = transactionId.split("_")[0];
            Long bookingId = Long.parseLong(bookingIdStr);

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking!"));

            Payment latestPayment = paymentRepository.findLatestByBooking(booking)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy payment!"));

            if ("00".equals(responseCode)) {
                // Thanh toán thành công
                latestPayment.setPaymentStatus(PaymentStatus.SUCCESS);
                latestPayment.setTransactionId(transactionId);
                paymentRepository.save(latestPayment);

                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                // CẬP NHẬT TRẠNG THÁI ROOM THÀNH OCCUPIED
                Room room = booking.getRoom();
                room.setStatus(RoomStatus.OCCUPIED);
                roomRepository.save(room);

                // GỬI EMAIL XÁC NHẬN
                try {
                    mailService.sendBookingConfirmation(booking);
                    log.info("Sent booking confirmation email to: {}", booking.getUser().getEmail());
                } catch (Exception e) {
                    log.error("Failed to send booking confirmation email", e);
                }

                log.info("Payment SUCCESS for booking: {}, Room {} set to OCCUPIED",
                        bookingId, room.getRoomNumber());
                return true;

            } else {
                // Thanh toán thất bại -> hủy vĩnh viễn
                latestPayment.setPaymentStatus(PaymentStatus.FAILED);
                latestPayment.setTransactionId(transactionId);
                paymentRepository.save(latestPayment);

                booking.setStatus(BookingStatus.CANCELLED_PERMANENTLY);
                bookingRepository.save(booking);

                log.warn("Payment FAILED for booking: {} - Status: CANCELLED_PERMANENTLY", bookingId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error processing payment callback", e);
            return false;
        }
    }

    private Payment createNewPayment(Booking booking) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        // Tạo transactionId tạm thời duy nhất
        payment.setTransactionId("PENDING_" + booking.getBookingId() + "_" + System.currentTimeMillis());
        payment.setExpireTime(LocalDateTime.now().plusMinutes(15));
        return paymentRepository.save(payment);
    }

    private String buildVNPayUrl(Booking booking, Payment payment, HttpServletRequest request)
            throws UnsupportedEncodingException {

        String vnp_TxnRef = booking.getBookingId() + "_" + payment.getPaymentId();
        long amount = booking.getTotalPrice().multiply(new BigDecimal(100)).longValue();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVnpVersion());
        vnp_Params.put("vnp_Command", vnPayConfig.getVnpCommand());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan booking " + booking.getBookingId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnp_Params.put("vnp_IpAddr", getIpAddress(request));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        String queryUrl = VNPayUtil.hashAllFields(vnp_Params, vnPayConfig.getVnpHashSecret());
        return vnPayConfig.getVnpUrl() + "?" + queryUrl;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        return ipAddress != null ? ipAddress : request.getRemoteAddr();
    }


    @Scheduled(fixedRate = 30000) // 30 seconds
    @Transactional
    public void cancelExpiredPendingBookings() {
        try {
            LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(15);

            List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings(
                    BookingStatus.PENDING, expiryTime);

            if (!expiredBookings.isEmpty()) {
                for (Booking booking : expiredBookings) {
                    // Cập nhật payment status thành FAILED
                    Payment latestPayment = paymentRepository.findLatestByBooking(booking)
                            .orElse(null);

                    if (latestPayment != null && latestPayment.getPaymentStatus() == PaymentStatus.PENDING) {
                        latestPayment.setPaymentStatus(PaymentStatus.FAILED);
                        paymentRepository.save(latestPayment);
                    }

                    // Hủy booking vĩnh viễn
                    booking.setStatus(BookingStatus.CANCELLED_PERMANENTLY);
                    bookingRepository.save(booking);

                    log.info("Auto-cancelled expired booking: {} (created at: {})",
                            booking.getBookingId(), booking.getCreatedAt());
                }

                log.info("Cancelled {} expired pending bookings", expiredBookings.size());
            }
        } catch (Exception e) {
            log.error("Error cancelling expired pending bookings", e);
        }
    }


}

