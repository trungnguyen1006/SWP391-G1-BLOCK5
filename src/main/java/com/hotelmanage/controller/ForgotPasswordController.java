package com.hotelmanage.controller;

import com.hotelmanage.entity.Enum.UserStatus;
import com.hotelmanage.entity.User;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.service.MailService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    static final String ATTR_EMAIL       = "fp_email";
    static final String ATTR_OTP         = "fp_otp";
    static final String ATTR_OTP_EXPIRES = "fp_otp_expires";
    static final String ATTR_OTP_ATTEMPTS = "fp_otp_attempts";
    static final int    MAX_OTP_ATTEMPTS  = 5;

    @GetMapping
    public String stepEmail(Model model) {
        model.addAttribute("emailForm", new EmailForm());
        model.addAttribute("step", 1);
        return "auth/forgot/email";
    }

    @PostMapping
    public String submitEmail(@Valid @ModelAttribute("emailForm") EmailForm form,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model) {
        Optional<User> userOpt = userRepository.findByEmail(form.getEmail());

        if (userOpt.isEmpty()) {
            bindingResult.rejectValue("email", "email.notfound", "Email không tồn tại");
        } else if (userOpt.get().getStatus() != UserStatus.ACTIVE) {
            bindingResult.rejectValue("email", "email.inactive", "Tài khoản này đã bị vô hiệu hóa");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("step", 1);
            return "auth/forgot/email";
        }

        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        session.setAttribute(ATTR_EMAIL, form.getEmail());
        session.setAttribute(ATTR_OTP, otp);
        session.setAttribute(ATTR_OTP_EXPIRES, Instant.now().plusSeconds(300));
        session.setAttribute(ATTR_OTP_ATTEMPTS, 0);

        mailService.sendOtp(form.getEmail(), otp);

        model.addAttribute("otpForm", new OtpForm());
        model.addAttribute("step", 2);
        return "auth/forgot/otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute("otpForm") OtpForm form,
                            BindingResult bindingResult,
                            HttpSession session,
                            Model model) {
        Object expect  = session.getAttribute(ATTR_OTP);
        Object exp     = session.getAttribute(ATTR_OTP_EXPIRES);
        Object attObj  = session.getAttribute(ATTR_OTP_ATTEMPTS);
        int attempts   = (attObj != null) ? (Integer) attObj : 0;

        if (expect == null || exp == null || Instant.now().isAfter((Instant) exp)) {
            bindingResult.reject("otp.expired", "Mã OTP đã hết hạn, vui lòng gửi lại");
        } else if (!String.valueOf(expect).equals(form.getOtp())) {
            attempts++;
            session.setAttribute(ATTR_OTP_ATTEMPTS, attempts);
            if (attempts >= MAX_OTP_ATTEMPTS) {
                session.invalidate();
                bindingResult.reject("otp.locked", "Bạn đã nhập sai quá 5 lần. Vui lòng thực hiện lại từ đầu.");
            } else {
                int remaining = MAX_OTP_ATTEMPTS - attempts;
                bindingResult.rejectValue("otp", "otp.invalid",
                        "Mã OTP không đúng. Còn " + remaining + " lần thử.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("step", 2);
            return "auth/forgot/otp";
        }

        model.addAttribute("resetForm", new ResetForm());
        model.addAttribute("step", 3);
        return "auth/forgot/reset";
    }

    @PostMapping("/reset")
    public String doReset(@Valid @ModelAttribute("resetForm") ResetForm form,
                          BindingResult bindingResult,
                          HttpSession session) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Mật khẩu không khớp");
        }
        String email = (String) session.getAttribute(ATTR_EMAIL);
        if (email == null) {
            bindingResult.reject("flow.invalid", "Phiên đặt lại mật khẩu không hợp lệ");
        }
        if (bindingResult.hasErrors()) {
            return "auth/forgot/reset";
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);

        session.invalidate();
        return "redirect:/login?reset";
    }

    @Data
    public static class EmailForm {
        @NotBlank
        @Email
        private String email;
    }

    @Data
    public static class OtpForm {
        @NotBlank(message = "Vui lòng nhập mã OTP")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải gồm đúng 6 chữ số")
        private String otp;
    }

    @Data
    public static class ResetForm {
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
        @Pattern(regexp = "^\\S+$", message = "Mật khẩu không được chứa dấu cách")
        private String password;

        @NotBlank(message = "Vui lòng xác nhận mật khẩu")
        private String confirmPassword;
    }
}
