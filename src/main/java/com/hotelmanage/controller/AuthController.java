package com.hotelmanage.controller;

import com.hotelmanage.entity.Enum.UserRole;
import com.hotelmanage.entity.Enum.UserStatus;
import com.hotelmanage.entity.User;
import com.hotelmanage.entity.booking.Booking;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.booking.BookingRepository;
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

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingRepository bookingRepository;

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
                             Model model,
                             RedirectAttributes redirectAttributes) {

        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(req.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Tên đăng nhập đã tồn tại");
        }

        // Kiểm tra số điện thoại đã được sử dụng (bỏ qua tài khoản GUEST tạm thời)
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            if (userRepository.existsByPhoneAndRoleNot(req.getPhone(), UserRole.GUEST)) {
                bindingResult.rejectValue("phone", "phone.exists", "Số điện thoại đã được sử dụng");
            }
        }

        // Kiểm tra email đã được dùng bởi CUSTOMER/ADMIN/RECEPTIONIST
        Optional<User> existingUserOpt = userRepository.findByEmail(req.getEmail());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Nếu là GUEST -> cho phép nâng cấp
            if (existingUser.getRole() == UserRole.GUEST) {
                log.info("Found existing GUEST account with email: {}, upgrading to CUSTOMER", req.getEmail());

                if (bindingResult.hasFieldErrors("username")) {
                    return "auth/register";
                }

                upgradeGuestToCustomer(existingUser, req);

                redirectAttributes.addFlashAttribute("success",
                        "Tài khoản của bạn đã được nâng cấp thành công! Lịch sử đặt phòng đã được liên kết.");
                return "redirect:/login?upgraded";
            } else {
                bindingResult.rejectValue("email", "email.exists", "Email đã được sử dụng");
            }
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("Created new CUSTOMER account: {}", req.getUsername());

        return "redirect:/login?registered";
    }

    private void upgradeGuestToCustomer(User guestUser, RegisterRequest req) {
        guestUser.setUsername(req.getUsername());
        guestUser.setPassword(passwordEncoder.encode(req.getPassword()));
        guestUser.setRole(UserRole.CUSTOMER);
        guestUser.setStatus(UserStatus.ACTIVE);

        if (req.getPhone() != null && !req.getPhone().isEmpty()) {
            guestUser.setPhone(req.getPhone());
        }
        if (req.getAddress() != null && !req.getAddress().isEmpty()) {
            guestUser.setAddress(req.getAddress());
        }

        userRepository.save(guestUser);

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
}
