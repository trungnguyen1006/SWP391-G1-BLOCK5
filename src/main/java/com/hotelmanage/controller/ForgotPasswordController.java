package com.hotelmanage.controller;


import com.hotelmanage.entity.Enum.UserStatus;
import com.hotelmanage.entity.User;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.service.MailService;
import jakarta.servlet.http.HttpSession;
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

import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    static final String ATTR_EMAIL = "fp_email";
    static final String ATTR_OTP = "fp_otp";
    static final String ATTR_OTP_EXPIRES = "fp_otp_expires";
    static final String ATTR_OTP_FAILED_ATTEMPTS = "fp_otp_failed_attempts";
    static final int MAX_OTP_FAILED_ATTEMPTS = 5;

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
            bindingResult.rejectValue("email", "account.inactive", "Tài khoản chưa được kích hoạt hoặc đã bị khóa");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("step", 1);
            return "auth/forgot/email";
        }

        // Generate OTP (6 digits) and store in session with short TTL
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        session.setAttribute(ATTR_EMAIL, form.getEmail());
        session.setAttribute(ATTR_OTP, otp);
        session.setAttribute(ATTR_OTP_EXPIRES, Instant.now().plusSeconds(300)); // 5 minutes
        session.setAttribute(ATTR_OTP_FAILED_ATTEMPTS, 0);

        // Gửi OTP qua email
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
        Object expect = session.getAttribute(ATTR_OTP);
        Object exp = session.getAttribute(ATTR_OTP_EXPIRES);
        Integer failedAttempts = (Integer) session.getAttribute(ATTR_OTP_FAILED_ATTEMPTS);
        if (failedAttempts == null) {
            failedAttempts = 0;
        }

        if (failedAttempts >= MAX_OTP_FAILED_ATTEMPTS) {
            session.invalidate();
            bindingResult.reject("otp.too_many_attempts", "Bạn đã nhập sai OTP quá 5 lần. Vui lòng thực hiện lại từ đầu.");
        } else if (expect == null || exp == null || Instant.now().isAfter((Instant) exp)) {
            bindingResult.reject("otp.expired", "Mã OTP đã hết hạn, vui lòng gửi lại");
        } else if (!String.valueOf(expect).equals(form.getOtp())) {
            failedAttempts++;
            int remainingAttempts = MAX_OTP_FAILED_ATTEMPTS - failedAttempts;
            if (failedAttempts >= MAX_OTP_FAILED_ATTEMPTS) {
                session.invalidate();
                bindingResult.reject("otp.too_many_attempts", "Bạn đã nhập sai OTP quá 5 lần. Vui lòng thực hiện lại từ đầu.");
            } else {
                session.setAttribute(ATTR_OTP_FAILED_ATTEMPTS, failedAttempts);
                bindingResult.rejectValue("otp", "otp.invalid", "Mã OTP không đúng. Bạn còn " + remainingAttempts + " lần thử.");
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
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
            userRepository.save(user);
        });
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
        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải gồm đúng 6 chữ số")
        private String otp;
    }

    @Data
    public static class ResetForm {
        @NotBlank
        @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
        @Pattern(regexp = "^\\S+$", message = "Mật khẩu không được chứa khoảng trắng")
        private String password;
        @NotBlank
        @Size(min = 6, max = 50, message = "Mật khẩu xác nhận phải từ 6 đến 50 ký tự")
        @Pattern(regexp = "^\\S+$", message = "Mật khẩu xác nhận không được chứa khoảng trắng")
        private String confirmPassword;
    }
}


