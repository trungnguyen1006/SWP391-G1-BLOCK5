package com.hotelmanage.controller.admin;

import com.hotelmanage.entity.Enum.UserRole;
import com.hotelmanage.entity.Enum.UserStatus;
import com.hotelmanage.entity.User;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/receptionists")
@RequiredArgsConstructor
public class ReceptionistController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String getReceptionists(Model model) {
        List<User> list = userService.getReceptionists();
        model.addAttribute("receptionists", list);
        return "admin/receptionist/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("receptionist", new User());
        return "admin/receptionist/form";
    }

    @PostMapping("/create")
    public String createReceptionist(@Valid @ModelAttribute("receptionist") User receptionist,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.existsByUsername(receptionist.getUsername())) {
                bindingResult.rejectValue("username", "username.exists", "Tên đăng nhập đã tồn tại");
            }
            if (userRepository.existsByEmail(receptionist.getEmail())) {
                bindingResult.rejectValue("email", "email.exists", "Email đã tồn tại");
            }
            if (bindingResult.hasErrors()) {
                model.addAttribute("receptionist", receptionist);
                return "admin/receptionist/form"; // quay lại form, không redirect
            }
                User user = User.builder()
                    .username(receptionist.getUsername())
                    .password(passwordEncoder.encode("Abc@123")) //default pass
                    .email(receptionist.getEmail())
                    .phone(receptionist.getPhone())
                    .address(receptionist.getAddress())
                    .role(UserRole.RECEPTIONIST)
                    .status(UserStatus.ACTIVE)
                    .build();
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Thêm lễ tân thành công!");
                return "redirect:/admin/receptionists";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/receptionists/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User u = userService.findById(id);
        model.addAttribute("receptionist", u);
        return "admin/receptionist/form";
    }

    @PostMapping("/update/{id}")
    public String updateReceptionist(@PathVariable Long id,
                                @Valid @ModelAttribute("receptionist") User receptionistForm,
                                BindingResult bindingResult,
                                     Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userService.findById(id);

            // Kiểm tra username trùng nhưng bỏ qua chính nó
            if (!existingUser.getUsername().equals(receptionistForm.getUsername())
                    && userRepository.existsByUsername(receptionistForm.getUsername())) {
                bindingResult.rejectValue("username", "username.exists", "Tên đăng nhập đã tồn tại");
            }

            // Kiểm tra email trùng
            if (!existingUser.getEmail().equals(receptionistForm.getEmail())
                    && userRepository.existsByEmail(receptionistForm.getEmail())) {
                bindingResult.rejectValue("email", "email.exists", "Email đã tồn tại");
            }

            if (bindingResult.hasErrors()) {
                return "admin/receptionist/form";
            }

            // Update field
            existingUser.setUsername(receptionistForm.getUsername());
            existingUser.setEmail(receptionistForm.getEmail());
            existingUser.setPhone(receptionistForm.getPhone());
            existingUser.setAddress(receptionistForm.getAddress());
            userRepository.save(existingUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật lễ tân thành công!");
            return "redirect:/admin/receptionists";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/receptionists/create";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleReceptionistStatus(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            userService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/receptionists";
    }

}
