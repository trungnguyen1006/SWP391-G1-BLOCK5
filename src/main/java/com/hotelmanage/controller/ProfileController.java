package com.hotelmanage.controller;

import com.hotelmanage.entity.User;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.service.CloudinaryService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile")
    public String profilePage(Model model) {
        User user = getCurrentUser().orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "auth/profile";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile avatarFile,
                               Model model) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        if (avatarFile == null || avatarFile.isEmpty()) {
            return "redirect:/profile?avatarError";
        }
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
    public String changePassword(@ModelAttribute("changePasswordForm") ChangePasswordForm form,
                                 BindingResult bindingResult,
                                 Model model) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            bindingResult.reject("auth.required", "Bạn cần đăng nhập");
        } else {
            User user = userOpt.get();
            if (form.getOldPassword() == null || !passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
                bindingResult.rejectValue("oldPassword", "password.invalid", "Mật khẩu hiện tại không đúng");
            }
            if (form.getPassword() == null || form.getPassword().length() < 6) {
                bindingResult.rejectValue("password", "password.short", "Mật khẩu tối thiểu 6 ký tự");
            }
            if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "password.mismatch", "Mật khẩu không khớp");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", getCurrentUser().orElse(null));
            return "auth/profile";
        }

        User user = getCurrentUser().get();
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);
        return "redirect:/profile?changed";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(value = "email", required = false) String email,
                                @RequestParam(value = "phone", required = false) String phone,
                                @RequestParam(value = "address", required = false) String address,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        // Update email if provided and not taken by others
        if (email != null && !email.isBlank()) {
            var existed = userRepository.findByEmail(email);
            if (existed.isPresent() && !existed.get().getId().equals(user.getId())) {
                return "redirect:/profile?updateError";
            }
            user.setEmail(email);
        }
        if (phone != null && !phone.isBlank()) {
            user.setPhone(phone);
        }
        if (address != null && !address.isBlank()) {
            user.setAddress(address);
        }
        // Optional avatar upload
        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String url = cloudinaryService.uploadImage(avatarFile, "avatars");
                if (url != null) {
                    user.setAvatarUrl(url);
                }
            }
        } catch (Exception e) {
            return "redirect:/profile?updateError";
        }
        userRepository.save(user);
        return "redirect:/profile?updated";
    }

    private Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return Optional.empty();
        return userRepository.findByUsername(auth.getName());
    }

    @Data
    public static class ChangePasswordForm {
        @NotBlank
        private String oldPassword;
        @NotBlank
        private String password;
        @NotBlank
        private String confirmPassword;
    }
}


