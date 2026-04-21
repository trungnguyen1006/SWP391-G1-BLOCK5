package com.hotelmanage.controller.admin;

import com.hotelmanage.entity.booking.Promotion;
import com.hotelmanage.service.booking.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public String listPromotions(Model model) {
        List<Promotion> promotions = promotionService.findAll();
        model.addAttribute("promotions", promotions);
        return "admin/promotion/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        return "admin/promotion/form";
    }

    @PostMapping("/create")
    public String createPromotion(@ModelAttribute Promotion promotion,
                                  RedirectAttributes redirectAttributes) {
        try {
            promotionService.save(promotion);
            redirectAttributes.addFlashAttribute("success", "Tạo mã giảm giá thành công!");
            return "redirect:/admin/promotions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/promotions/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Promotion promotion = promotionService.findById(id);
        model.addAttribute("promotion", promotion);
        return "admin/promotion/form";
    }

    @PostMapping("/edit/{id}")
    public String updatePromotion(@PathVariable Integer id,
                                  @ModelAttribute Promotion promotion,
                                  RedirectAttributes redirectAttributes) {
        try {
            promotion.setPromotionId(id);
            promotionService.update(promotion);
            redirectAttributes.addFlashAttribute("success", "Cập nhật mã giảm giá thành công!");
            return "redirect:/admin/promotions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/promotions/edit/" + id;
        }
    }

    @PostMapping("/deactivate/{id}")
    public String deactivatePromotion(@PathVariable Integer id,
                                      RedirectAttributes redirectAttributes) {
        try {
            promotionService.deactivate(id);
            redirectAttributes.addFlashAttribute("success", "Vô hiệu hóa mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @PostMapping("/activate/{id}")
    public String activatePromotion(@PathVariable Integer id,
                                    RedirectAttributes redirectAttributes) {
        try {
            Promotion promotion = promotionService.findById(id);
            promotion.setIsActive(true);
            promotionService.save(promotion);
            redirectAttributes.addFlashAttribute("success", "Kích hoạt mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }
}
