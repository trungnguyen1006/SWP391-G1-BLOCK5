package com.hotelmanage.controller.restaurant;

import com.hotelmanage.entity.User;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.service.CloudinaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PHONE_REGEX = "^(\\+84|0)[0-9]{9}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile")
    public String profilePage(Model model) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
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
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
                                 BindingResult bindingResult,
                                 Model model) {
        Optional<User> userOpt = getCurrentUser();
        User user = userOpt.orElse(null);
        if (userOpt.isEmpty()) {
            bindingResult.reject("auth.required", "Bạn cần đăng nhập");
        } else {
            if (form.getOldPassword() == null || !passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
                bindingResult.rejectValue("oldPassword", "password.invalid", "Mật khẩu hiện tại không đúng");
            }
            if (form.getPassword() != null && passwordEncoder.matches(form.getPassword(), user.getPassword())) {
                bindingResult.rejectValue("password", "password.same_as_old", "Mật khẩu mới không được trùng mật khẩu cũ");
            }
            if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "password.mismatch", "Mật khẩu không khớp");
            }
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
            if (!email.matches(EMAIL_REGEX)) {
                return "redirect:/profile?updateError";
            }
            var existed = userRepository.findByEmail(email);
            if (existed.isPresent() && !existed.get().getId().equals(user.getId())) {
                return "redirect:/profile?updateError";
            }
            user.setEmail(email);
        }
        if (phone != null && !phone.isBlank()) {
            if (!phone.matches(PHONE_REGEX)) {
                return "redirect:/profile?updateError";
            }
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
        @Size(min = 6, max = 50)
        @Pattern(regexp = "^\\S+$")
        private String oldPassword;
        @NotBlank
        @Size(min = 6, max = 50)
        @Pattern(regexp = "^\\S+$")
        private String password;
        @NotBlank
        @Size(min = 6, max = 50)
        @Pattern(regexp = "^\\S+$")
        private String confirmPassword;
    }
}


