package com.hotelmanage.controller.admin;


import com.hotelmanage.entity.Enum.UserRole;
import com.hotelmanage.entity.Enum.UserStatus;
import com.hotelmanage.entity.User;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.service.CloudinaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> userPage;

        if (query != null && !query.trim().isEmpty()) {
            userPage = userRepository.findByRoleNotAndUsernameContainingIgnoreCaseOrRoleNotAndEmailContainingIgnoreCase(
                    UserRole.ADMIN, query.trim(), UserRole.ADMIN, query.trim(), pageable);
        } else {
            userPage = userRepository.findByRoleNot(UserRole.ADMIN, pageable);
        }

        model.addAttribute("page", userPage);
        model.addAttribute("q", query);
        return "admin/users/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isEdit", false);
        return "admin/users/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute UserForm form,
                         BindingResult bindingResult,
                         @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                         Model model) {
        // Validation
        if (userRepository.existsByUsername(form.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            bindingResult.rejectValue("email", "email.exists", "Email đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            User user = new User();
            user.setUsername(form.getUsername());
            user.setEmail(form.getEmail());
            user.setPhone(form.getPhone());
            user.setAddress(form.getAddress());
            user.setRole(form.getRole());
            model.addAttribute("user", user);
            model.addAttribute("isEdit", false);
            return "admin/users/form";
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setAddress(form.getAddress());
        user.setRole(form.getRole());
        user.setStatus(UserStatus.ACTIVE);

        // Upload avatar if provided
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars");
                user.setAvatarUrl(avatarUrl);
            } catch (IOException e) {
                model.addAttribute("user", user);
                model.addAttribute("isEdit", false);
                model.addAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
                return "admin/users/form";
            }
        }

        userRepository.save(user);
        return "redirect:/admin/users?success";
    }

    @GetMapping("/{id}/view")
    public String view(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/users?notFound";
        }
        model.addAttribute("user", user);
        return "admin/users/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/users?notFound";
        }
        model.addAttribute("user", user);
        model.addAttribute("isEdit", true);
        return "admin/users/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute UserEditForm form,
                         BindingResult bindingResult,
                         @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                         Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/users?notFound";
        }

        // Check if username changed and exists
        if (!user.getUsername().equals(form.getUsername()) && userRepository.existsByUsername(form.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Tên đăng nhập đã tồn tại");
        }
        // Check if email changed and exists
        if (!user.getEmail().equals(form.getEmail()) && userRepository.existsByEmail(form.getEmail())) {
            bindingResult.rejectValue("email", "email.exists", "Email đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            user.setUsername(form.getUsername());
            user.setEmail(form.getEmail());
            user.setPhone(form.getPhone());
            user.setAddress(form.getAddress());
            user.setRole(form.getRole());
            model.addAttribute("user", user);
            model.addAttribute("isEdit", true);
            return "admin/users/form";
        }

        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setAddress(form.getAddress());
        user.setRole(form.getRole());

        // Upload new avatar if provided
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars");
                user.setAvatarUrl(avatarUrl);
            } catch (IOException e) {
                model.addAttribute("user", user);
                model.addAttribute("isEdit", true);
                model.addAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
                return "admin/users/form";
            }
        }

        userRepository.save(user);
        return "redirect:/admin/users?updated";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);
        }
        return "redirect:/admin/users?deactivated";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }
        return "redirect:/admin/users?activated";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users?deleted";
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserForm {
        @NotBlank(message = "Tên đăng nhập không được để trống")
        String username;

        @NotBlank(message = "Mật khẩu không được để trống")
        String password;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email;

        String phone;
        String address;

        UserRole role = UserRole.CUSTOMER;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserEditForm {
        @NotBlank(message = "Tên đăng nhập không được để trống")
        String username;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email;

        String phone;
        String address;

        UserRole role = UserRole.CUSTOMER;
    }
}

