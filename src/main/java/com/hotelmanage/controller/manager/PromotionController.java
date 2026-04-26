package com.hotelmanage.controller.manager;

import com.hotelmanage.entity.booking.Promotion;
import com.hotelmanage.service.booking.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manager/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public String listPromotions(@RequestParam(value = "q", required = false) String keyword,
                                 @RequestParam(value = "sort", required = false) String sortDirection,
                                 Model model) {
        List<Promotion> promotions = promotionService.searchAndSort(keyword, sortDirection);
        model.addAttribute("promotions", promotions);
        model.addAttribute("q", keyword);
        model.addAttribute("sort", sortDirection);
        return "manager/promotion/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        return "manager/promotion/form";
    }

    @PostMapping("/create")
    public String createPromotion(@Valid @ModelAttribute("promotion") Promotion promotion,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "manager/promotion/form";
        }
        if (promotion.getIsActive() == null) {
            promotion.setIsActive(false);
        }
        try {
            promotionService.save(promotion);
            redirectAttributes.addFlashAttribute("success", "Tạo mã giảm giá thành công!");
            return "redirect:/manager/promotions";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "manager/promotion/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Promotion promotion = promotionService.findById(id);
        model.addAttribute("promotion", promotion);
        return "manager/promotion/form";
    }

    @PostMapping("/edit/{id}")
    public String updatePromotion(@PathVariable Integer id,
                                  @Valid @ModelAttribute("promotion") Promotion promotion,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            promotion.setPromotionId(id);
            return "manager/promotion/form";
        }
        if (promotion.getIsActive() == null) {
            promotion.setIsActive(false);
        }
        try {
            promotion.setPromotionId(id);
            promotionService.update(promotion);
            redirectAttributes.addFlashAttribute("success", "Cập nhật mã giảm giá thành công!");
            return "redirect:/manager/promotions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/manager/promotions/edit/" + id;
        }
    }
}
