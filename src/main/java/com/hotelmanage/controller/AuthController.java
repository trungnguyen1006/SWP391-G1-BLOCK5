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
                             Model model) {

        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(req.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Tên đăng nhập đã tồn tại");
        }

        // Kiểm tra email đã được dùng bởi CUSTOMER/ADMIN/RECEPTIONIST
        Optional<User> existingUserOpt = userRepository.findByEmail(req.getEmail());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Nếu là GUEST -> cho phép nâng cấp
            if (existingUser.getRole() == UserRole.GUEST) {
                log.info("Found existing GUEST account with email: {}, upgrading to CUSTOMER", req.getEmail());

                // Kiểm tra username mới không trùng
                if (bindingResult.hasFieldErrors("username")) {
                    return "auth/register";
                }

                // Nâng cấp GUEST lên CUSTOMER
                upgradeGuestToCustomer(existingUser, req);

                model.addAttribute("success", "Tài khoản của bạn đã được nâng cấp thành công! Lịch sử đặt phòng đã được liên kết.");
                return "redirect:/login?upgraded";
            } else {
                // Nếu là CUSTOMER/ADMIN/RECEPTIONIST -> báo lỗi
                bindingResult.rejectValue("email", "email.exists", "Email đã được sử dụng");
            }
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Tạo mới tài khoản CUSTOMER
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
        private String username;

        @NotBlank(message = "Mật khẩu không được để trống")
        private String password;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        private String email;

        private String phone;
        private String address;
    }
}
