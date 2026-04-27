package com.hotelmanage.controller.restaurant;

import com.hotelmanage.entity.Enum.UserRole;
import com.hotelmanage.entity.User;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.service.CloudinaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile")
    public String profilePage(Model model) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("user", userOpt.get());
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "auth/profile";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile avatarFile) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) return "redirect:/login";
        if (avatarFile == null || avatarFile.isEmpty()) return "redirect:/profile?avatarError";
        try {
            String url = cloudinaryService.uploadImage(avatarFile, "avatars");
            if (url != null) {
                User user = userOpt.get();
                user.setAvatarUrl(url);
                userRepository.save(user);
                return "redirect:/profile?avatarUpdated";
            }
            return "redirect:/profile?avatarError";
        } catch (Exception e) {
            return "redirect:/profile?avatarError";
        }
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
                                 BindingResult bindingResult,
                                 Model model) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
            bindingResult.rejectValue("oldPassword", "password.invalid", "Mật khẩu hiện tại không đúng");
        }

        if (!bindingResult.hasFieldErrors("oldPassword") &&
                passwordEncoder.matches(form.getPassword(), user.getPassword())) {
            bindingResult.rejectValue("password", "password.same", "Mật khẩu mới không được trùng mật khẩu cũ");
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Mật khẩu không khớp");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "auth/profile";
        }

        user.setPassword(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);
        return "redirect:/profile?changed";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(value = "email", required = false) String email,
                                @RequestParam(value = "address", required = false) String address,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                                RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) return "redirect:/login";

        User user = userOpt.get();

        // Validate và update email
        if (email != null && !email.isBlank()) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                redirectAttributes.addFlashAttribute("updateError", "Email không đúng định dạng");
                return "redirect:/profile";
            }
            var existed = userRepository.findByEmail(email);
            if (existed.isPresent() && !existed.get().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("updateError", "Email đã được sử dụng");
                return "redirect:/profile";
            }
            user.setEmail(email);
        }

        if (address != null && !address.isBlank()) {
            user.setAddress(address);
        } else if (address != null && address.isBlank()) {
            user.setAddress(null);
        }

        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String url = cloudinaryService.uploadImage(avatarFile, "avatars");
                if (url != null) user.setAvatarUrl(url);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("updateError", "Lỗi upload ảnh");
            return "redirect:/profile";
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("updateSuccess", "Cập nhật thông tin thành công!");
        return "redirect:/profile?updated";
    }

    private Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return Optional.empty();
        return userRepository.findByUsername(auth.getName());
    }

    @Data
    public static class ChangePasswordForm {
        @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
        private String oldPassword;

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
        @Pattern(regexp = "^\\S+$", message = "Mật khẩu không được chứa dấu cách")
        private String password;

        @NotBlank(message = "Vui lòng xác nhận mật khẩu")
        private String confirmPassword;
    }
}
