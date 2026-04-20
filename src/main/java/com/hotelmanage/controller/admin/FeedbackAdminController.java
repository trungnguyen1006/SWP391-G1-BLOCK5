package com.hotelmanage.controller.admin;

import com.hotelmanage.entity.Enum.FeedbackCategory;
import com.hotelmanage.entity.feedback.Feedback;
import com.hotelmanage.service.feedback.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/feedbacks")
@RequiredArgsConstructor
public class FeedbackAdminController {

    private final FeedbackService feedbackService;

    @GetMapping
    public String listFeedbacks(@RequestParam(required = false) FeedbackCategory category,
                                Model model) {
        List<Feedback> feedbacks = (category != null)
                ? feedbackService.findByCategory(category)
                : feedbackService.findAll();

        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", FeedbackCategory.values());
        model.addAttribute("pageTitle", "Quản lý góp ý");
        return "admin/feedback/list";
    }
}

