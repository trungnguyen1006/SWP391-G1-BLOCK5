package com.hotelmanage.controller;

import com.hotelmanage.entity.Enum.UserRole;
import com.hotelmanage.entity.Enum.UserStatus;
import com.hotelmanage.entity.User;
import com.hotelmanage.entity.booking.Booking;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.booking.BookingRepository;
import com.hotelmanage.service.MailService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingRepository bookingRepository;
    private final MailService mailService;

    static final String ATTR_REGISTER_PENDING = "register_pending";
    static final String ATTR_REGISTER_GUEST_ID = "register_guest_id";
    static final String ATTR_REGISTER_OTP = "register_otp";
    static final String ATTR_REGISTER_OTP_EXPIRES = "register_otp_expires";
    static final String ATTR_REGISTER_OTP_ATTEMPTS = "register_otp_attempts";
    static final int MAX_REGISTER_OTP_ATTEMPTS = 5;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    @Transactional
    public String doRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest req,
                             BindingResult bindingResult,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(req.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Tên đăng nhập đã tồn tại");
        }

        // Kiểm tra số điện thoại đã được sử dụng (bỏ qua tài khoản GUEST tạm thời)
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            if (userRepository.existsByPhoneAndRoleNot(req.getPhone(), UserRole.GUEST)) {
                bindingResult.rejectValue("phone", "phone.exists", "Số điện thoại đã tồn tại, vui lòng dùng số khác.");
            }
        }

        // Kiểm tra email đã được dùng bởi CUSTOMER/ADMIN/RECEPTIONIST
        Optional<User> existingUserOpt = userRepository.findByEmail(req.getEmail());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Nếu là GUEST -> cho phép nâng cấp
            if (existingUser.getRole() == UserRole.GUEST) {
                log.info("Found existing GUEST account with email: {}, upgrading to CUSTOMER", req.getEmail());

                prepareRegisterOtpSession(req, existingUser.getId(), session);
                mailService.sendOtp(req.getEmail(), (String) session.getAttribute(ATTR_REGISTER_OTP));
                redirectAttributes.addFlashAttribute("info", "Mã OTP đã được gửi đến email của bạn.");
                return "redirect:/register/otp?sent";
            } else {
                // Nếu là CUSTOMER/ADMIN/RECEPTIONIST -> báo lỗi
                bindingResult.rejectValue("email", "email.exists", "Email đã được sử dụng");
            }
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        prepareRegisterOtpSession(req, null, session);
        mailService.sendOtp(req.getEmail(), (String) session.getAttribute(ATTR_REGISTER_OTP));
        redirectAttributes.addFlashAttribute("info", "Mã OTP đã được gửi đến email của bạn.");
        return "redirect:/register/otp?sent";
    }

    @GetMapping("/register/otp")
    public String registerOtpPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        PendingRegisterRequest pending = (PendingRegisterRequest) session.getAttribute(ATTR_REGISTER_PENDING);
        if (pending == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên đăng ký không hợp lệ. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }
        model.addAttribute("otpForm", new OtpForm());
        model.addAttribute("registerEmail", pending.getEmail());
        return "auth/register-otp";
    }

    @PostMapping("/register/otp")
    @Transactional
    public String verifyRegisterOtp(@Valid @ModelAttribute("otpForm") OtpForm form,
                                    BindingResult bindingResult,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        PendingRegisterRequest pending = (PendingRegisterRequest) session.getAttribute(ATTR_REGISTER_PENDING);
        Object expectedOtp = session.getAttribute(ATTR_REGISTER_OTP);
        Object expiresAt = session.getAttribute(ATTR_REGISTER_OTP_EXPIRES);
        int attempts = (session.getAttribute(ATTR_REGISTER_OTP_ATTEMPTS) instanceof Integer)
                ? (Integer) session.getAttribute(ATTR_REGISTER_OTP_ATTEMPTS) : 0;

        if (pending == null || expectedOtp == null || expiresAt == null) {
            clearRegisterSession(session);
            redirectAttributes.addFlashAttribute("error", "Phiên OTP không hợp lệ. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }

        if (Instant.now().isAfter((Instant) expiresAt)) {
            bindingResult.reject("otp.expired", "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");
        } else if (!String.valueOf(expectedOtp).equals(form.getOtp())) {
            attempts++;
            session.setAttribute(ATTR_REGISTER_OTP_ATTEMPTS, attempts);
            if (attempts >= MAX_REGISTER_OTP_ATTEMPTS) {
                clearRegisterSession(session);
                redirectAttributes.addFlashAttribute("error", "Bạn đã nhập sai OTP quá 5 lần. Vui lòng đăng ký lại.");
                return "redirect:/register";
            }
            bindingResult.rejectValue("otp", "otp.invalid",
                    "Mã OTP không đúng. Còn " + (MAX_REGISTER_OTP_ATTEMPTS - attempts) + " lần thử.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("registerEmail", pending.getEmail());
            return "auth/register-otp";
        }

        Long guestId = (Long) session.getAttribute(ATTR_REGISTER_GUEST_ID);
        if (guestId != null) {
            User guestUser = userRepository.findById(guestId).orElse(null);
            if (guestUser != null && guestUser.getRole() == UserRole.GUEST) {
                upgradeGuestToCustomer(guestUser, pending.toRegisterRequest());
                log.info("Upgraded GUEST via OTP flow: {}", pending.getUsername());
            } else {
                clearRegisterSession(session);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản cần nâng cấp. Vui lòng đăng ký lại.");
                return "redirect:/register";
            }
        } else {
            User user = User.builder()
                    .username(pending.getUsername())
                    .password(passwordEncoder.encode(pending.getPassword()))
                    .email(pending.getEmail())
                    .phone(pending.getPhone())
                    .address(pending.getAddress())
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(user);
            log.info("Created new CUSTOMER account via OTP: {}", pending.getUsername());
        }

        clearRegisterSession(session);
        return "redirect:/login?registered";
    }

    @PostMapping("/register/resend-otp")
    public String resendRegisterOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        PendingRegisterRequest pending = (PendingRegisterRequest) session.getAttribute(ATTR_REGISTER_PENDING);
        if (pending == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên đăng ký không hợp lệ. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        session.setAttribute(ATTR_REGISTER_OTP, otp);
        session.setAttribute(ATTR_REGISTER_OTP_EXPIRES, Instant.now().plusSeconds(300));
        session.setAttribute(ATTR_REGISTER_OTP_ATTEMPTS, 0);
        mailService.sendOtp(pending.getEmail(), otp);
        redirectAttributes.addFlashAttribute("info", "Đã gửi lại mã OTP.");
        return "redirect:/register/otp?resent";
    }

    private void prepareRegisterOtpSession(RegisterRequest req, Long guestId, HttpSession session) {
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        PendingRegisterRequest pending = PendingRegisterRequest.builder()
                .username(req.getUsername())
                .password(req.getPassword())
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .build();
        session.setAttribute(ATTR_REGISTER_PENDING, pending);
        session.setAttribute(ATTR_REGISTER_GUEST_ID, guestId);
        session.setAttribute(ATTR_REGISTER_OTP, otp);
        session.setAttribute(ATTR_REGISTER_OTP_EXPIRES, Instant.now().plusSeconds(300));
        session.setAttribute(ATTR_REGISTER_OTP_ATTEMPTS, 0);
    }

    private void clearRegisterSession(HttpSession session) {
        session.removeAttribute(ATTR_REGISTER_PENDING);
        session.removeAttribute(ATTR_REGISTER_GUEST_ID);
        session.removeAttribute(ATTR_REGISTER_OTP);
        session.removeAttribute(ATTR_REGISTER_OTP_EXPIRES);
        session.removeAttribute(ATTR_REGISTER_OTP_ATTEMPTS);
    }

    /**
     * Nâng cấp tài khoản GUEST lên CUSTOMER
     */
    private void upgradeGuestToCustomer(User guestUser, RegisterRequest req) {
        // Cập nhật thông tin user
        guestUser.setUsername(req.getUsername());
        guestUser.setPassword(passwordEncoder.encode(req.getPassword()));
        guestUser.setRole(UserRole.CUSTOMER);
        guestUser.setStatus(UserStatus.ACTIVE);

        // Cập nhật thông tin bổ sung nếu có
        if (req.getPhone() != null && !req.getPhone().isEmpty()) {
            guestUser.setPhone(req.getPhone());
        }
        if (req.getAddress() != null && !req.getAddress().isEmpty()) {
            guestUser.setAddress(req.getAddress());
        }

        userRepository.save(guestUser);

        // Kiểm tra và log số booking đã liên kết
        List<Booking> bookings = bookingRepository.findByUserId(guestUser.getId());
        log.info("Upgraded GUEST to CUSTOMER: {} - Found {} existing bookings",
                req.getUsername(), bookings.size());
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 3, max = 20, message = "Tên đăng nhập phải từ 3 đến 20 ký tự")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$",
                message = "Tên đăng nhập chỉ được chứa chữ cái, chữ số và dấu gạch dưới")
        private String username;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
        @Pattern(regexp = "^\\S+$", message = "Mật khẩu không được chứa dấu cách")
        private String password;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        private String email;

        @Pattern(regexp = "^$|^(\\+84|0)[0-9]{9}$",
                message = "Số điện thoại không hợp lệ (VD: 0912345678 hoặc +84912345678)")
        private String phone;

        private String address;
    }

    @Data
    public static class OtpForm {
        @NotBlank(message = "Vui lòng nhập mã OTP")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải gồm đúng 6 chữ số")
        private String otp;
    }

    @Data
    @lombok.Builder
    public static class PendingRegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String address;

        public RegisterRequest toRegisterRequest() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername(username);
            req.setPassword(password);
            req.setEmail(email);
            req.setPhone(phone);
            req.setAddress(address);
            return req;
        }
    }
}
