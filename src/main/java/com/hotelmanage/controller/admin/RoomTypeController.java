package com.hotelmanage.controller.admin;

import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.repository.room.RoomTypeRepository;
import com.hotelmanage.service.room.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;
    private final RoomTypeRepository roomTypeRepository;

    @GetMapping
    public String listRoomTypes(@RequestParam(value = "q", required = false) String query,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                Model model) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.ASC, "roomTypeId"));
        Page<RoomType> roomTypePage;

        if (query != null && !query.trim().isEmpty()) {
            roomTypePage = roomTypeRepository.findByRoomTypeNameContainingIgnoreCaseAndDeletedAtIsNull(query.trim(), pageable);
        } else {
            roomTypePage = roomTypeRepository.findAllActive(pageable);
        }

        model.addAttribute("roomTypes", roomTypePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomTypePage.getTotalPages());
        model.addAttribute("q", query);

        return "admin/room-type-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("roomType", new RoomType());
        return "admin/room-type-form";
    }

    @PostMapping("/create")
    public String createRoomType(@Valid @ModelAttribute RoomType roomType,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        log.info("Creating room type: name={}, price={}, amountPerson={}",
                roomType.getRoomTypeName(), roomType.getPrice(), roomType.getAmountPerson());

        if (result.hasErrors()) {
            model.addAttribute("roomType", roomType);
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin nhập vào!");
            return "admin/room-type-form";
        }

        try {
            roomTypeService.save(roomType);
            redirectAttributes.addFlashAttribute("success", "Thêm loại phòng thành công!");
            return "redirect:/admin/room-types";
        } catch (Exception e) {
            log.error("Error creating room type", e);
            model.addAttribute("roomType", roomType);
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/room-type-form";
        }
    }




    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        RoomType roomType = roomTypeService.findById(id);
        model.addAttribute("roomType", roomType);
        return "admin/room-type-form";
    }

    @PostMapping("/update/{id}")
    public String updateRoomType(@PathVariable Integer id,
                                 @Valid @ModelAttribute RoomType roomType,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/room-type-form";
        }

        try {
            roomType.setRoomTypeId(id);
            roomTypeService.update(roomType);
            redirectAttributes.addFlashAttribute("success", "Cập nhật loại phòng thành công!");
            return "redirect:/admin/room-types";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/room-types/edit/" + id;
        }
    }


    /**
     * Xóa loại phòng (soft delete)
     */
    @PostMapping("/delete/{id}")
    public String deleteRoomType(@PathVariable Integer id,
                                 RedirectAttributes redirectAttributes) {
        try {
            roomTypeService.delete(id);
            redirectAttributes.addFlashAttribute("success",
                    "Đã xóa loại phòng thành công!");
        } catch (RuntimeException e) {
            log.error("Error deleting room type: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "" + e.getMessage());
        }
        return "redirect:/admin/room-types";
    }




}
