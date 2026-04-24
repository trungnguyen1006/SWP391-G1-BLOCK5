package com.hotelmanage.controller.roomtype;

import com.hotelmanage.entity.User;
import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.service.room.RoomTypeReviewService;
import com.hotelmanage.service.room.RoomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/room-types")
@RequiredArgsConstructor
@Slf4j
public class UserRoomTypeController {

    private final RoomTypeService roomTypeService;
    private final UserRepository userRepository;
    private final RoomTypeReviewService roomTypeReviewService;

    @GetMapping
    public String listRoomTypes(Model model) {
        List<RoomType> roomTypes = roomTypeService.findAll();
        model.addAttribute("roomTypes", roomTypes);
        return "users/room-type-list";
    }

    @GetMapping("/{id}")
    public String viewRoomTypeDetail(@PathVariable Integer id, Model model, Principal principal) {
        RoomType roomType = roomTypeService.findById(id);
        User currentUser = null;
        if (principal != null) {
            currentUser = userRepository.findByUsername(principal.getName())
                    .orElse(null);
            model.addAttribute("currentUser", currentUser);
        if (currentUser != null) {
            roomTypeReviewService.findUserReview(id, currentUser.getId())
                    .ifPresent(review -> model.addAttribute("userReview", review));
        }
    }

        model.addAttribute("reviews", roomTypeReviewService.getReviewsForRoomType(id));
        model.addAttribute("averageRating", roomTypeReviewService.calculateAverageRating(id));
        model.addAttribute("totalReviews", roomTypeReviewService.countReviews(id));
        model.addAttribute("roomType", roomType);
        return "users/room-type-detail";
    }

    @PostMapping("/{roomTypeId}/reviews")
    public String submitReview(@PathVariable Integer roomTypeId,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String comment,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        if (principal == null) {
            redirectAttributes.addFlashAttribute("reviewError", "Vui lòng đăng nhập để đánh giá phòng.");
            return buildRedirect(roomTypeId);
        }

        try {
            roomTypeReviewService.submitReview(roomTypeId, principal.getName(), rating, comment);
            redirectAttributes.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã chia sẻ trải nghiệm!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("reviewError", ex.getMessage());
        } catch (Exception ex) {
            log.error("Không thể lưu đánh giá", ex);
            redirectAttributes.addFlashAttribute("reviewError", "Đã có lỗi xảy ra, vui lòng thử lại sau.");
        }

        return buildRedirect(roomTypeId);
    }
    private String buildRedirect(Integer roomTypeId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/room-types/{roomTypeId}");
        return "redirect:" + builder.buildAndExpand(roomTypeId).toUriString();
    }
}
