package com.hotelmanage.controller.feedback;

import com.hotelmanage.entity.Enum.FeedbackCategory;
import com.hotelmanage.entity.restaurant.Restaurant;
import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.repository.restaurant.RestaurantRepository;
import com.hotelmanage.service.feedback.FeedbackService;
import com.hotelmanage.service.room.RoomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final RestaurantRepository restaurantRepository;
    private final RoomTypeService roomTypeService;

    @GetMapping
    public String showForm(Model model) {
        prepareReferenceData(model);
        if (!model.containsAttribute("feedbackForm")) {
            model.addAttribute("feedbackForm", new FeedbackForm());
        }
        return "feedback/form";
    }

    @PostMapping
    public String submitFeedback(@ModelAttribute("feedbackForm") FeedbackForm form,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để gửi góp ý.");
            return "redirect:/auth/login";
        }

        validateForm(form, bindingResult);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.feedbackForm", bindingResult);
            redirectAttributes.addFlashAttribute("feedbackForm", form);
            return "redirect:/feedback";
        }

        try {
            feedbackService.submitFeedback(
                    principal.getName(),
                    form.getCategory(),
                    form.getRestaurantId(),
                    form.getRoomTypeId(),
                    form.getContent()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi góp ý cho chúng tôi!");
        } catch (IllegalArgumentException e) {
            log.warn("Không thể lưu feedback: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("feedbackForm", form);
        } catch (Exception e) {
            log.error("Unexpected error while saving feedback", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã có lỗi xảy ra. Vui lòng thử lại sau.");
        }

        return "redirect:/feedback";
    }

    @ModelAttribute("categories")
    public FeedbackCategory[] categories() {
        return FeedbackCategory.values();
    }

    @ModelAttribute("restaurants")
    public List<Restaurant> restaurants() {
        return restaurantRepository.findAllByOrderByNameAsc();
    }

    @ModelAttribute("roomTypes")
    public List<RoomType> roomTypes() {
        return roomTypeService.findAll();
    }

    private void prepareReferenceData(Model model) {
        model.addAttribute("pageTitle", "Góp Ý Dịch Vụ");
    }

    private void validateForm(FeedbackForm form, BindingResult bindingResult) {
        if (form.getCategory() == null) {
            bindingResult.rejectValue("category", "category.required", "Vui lòng chọn danh mục góp ý.");
        }

        if (form.getCategory() == FeedbackCategory.BOOKING_SERVICE
                || form.getCategory() == FeedbackCategory.BOTH) {
            if (form.getRoomTypeId() == null) {
                bindingResult.rejectValue("roomTypeId", "roomTypeId.required", "Vui lòng chọn dịch vụ đặt phòng muốn góp ý.");
            }
        } else {
            form.setRoomTypeId(null);
        }

        if (form.getCategory() == FeedbackCategory.RESTAURANT
                || form.getCategory() == FeedbackCategory.BOTH) {
            if (form.getRestaurantId() == null) {
                bindingResult.rejectValue("restaurantId", "restaurantId.required", "Vui lòng chọn nhà hàng muốn góp ý.");
            }
        } else {
            form.setRestaurantId(null);
        }

        if (form.getContent() == null || form.getContent().trim().length() < 10) {
            bindingResult.rejectValue("content", "content.minlength", "Vui lòng nhập tối thiểu 10 ký tự.");
        } else if (form.getContent().trim().length() > 2000) {
            bindingResult.rejectValue("content", "content.maxlength", "Nội dung góp ý không vượt quá 2000 ký tự.");
        } else {
            form.setContent(form.getContent().trim());
        }
    }

    public static class FeedbackForm {
        @NotNull
        private FeedbackCategory category;

        private Long restaurantId;

        private Integer roomTypeId;

        @NotBlank
        @Size(min = 10, max = 2000)
        private String content;

        public FeedbackCategory getCategory() {
            return category;
        }

        public void setCategory(FeedbackCategory category) {
            this.category = category;
        }

        public Long getRestaurantId() {
            return restaurantId;
        }

        public void setRestaurantId(Long restaurantId) {
            this.restaurantId = restaurantId;
        }

        public Integer getRoomTypeId() {
            return roomTypeId;
        }

        public void setRoomTypeId(Integer roomTypeId) {
            this.roomTypeId = roomTypeId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

