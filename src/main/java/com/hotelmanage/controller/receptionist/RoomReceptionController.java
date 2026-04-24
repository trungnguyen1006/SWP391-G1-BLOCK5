package com.hotelmanage.controller.receptionist;

import com.hotelmanage.entity.room.Room;
import com.hotelmanage.repository.room.RoomRepository;
import com.hotelmanage.service.room.RoomService;
import com.hotelmanage.service.room.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reception")
@RequiredArgsConstructor
public class RoomReceptionController {

    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final RoomTypeService roomTypeService;

    @GetMapping("/rooms")
    public String listRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Room> roomPage;

        // Tìm kiếm theo keyword nếu có, ngược lại lấy tất cả
        if (keyword != null && !keyword.trim().isEmpty()) {
            roomPage = roomRepository.searchActiveRooms(keyword.trim(), pageable);
        } else {
            roomPage = roomRepository.findAllActive(pageable);
        }

        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("currentPage", roomPage.getNumber());
        model.addAttribute("totalPages", roomPage.getTotalPages());
        model.addAttribute("totalItems", roomPage.getTotalElements());
        model.addAttribute("keyword", keyword); // Giữ lại giá trị search

        return "receptionist/room/list";
    }

    @GetMapping("/update/{id}/rooms")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Room room = roomService.findById(id);
        model.addAttribute("room", room);
        model.addAttribute("roomTypes", roomTypeService.findAll());
        return "receptionist/room/update";
    }

    @PostMapping("/update/{id}/rooms")
    public String updateRoom(@PathVariable Integer id,
                             @Valid @ModelAttribute("room") Room room,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roomTypes", roomTypeService.findAll());
            return "receptionist/room/update";
        }
        try {
            // Lấy room hiện tại từ DB
            Room existingRoom = roomService.findById(id);

            // Chỉ cập nhật trạng thái, giữ nguyên các field khác
            existingRoom.setStatus(room.getStatus());

            roomService.update(existingRoom);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái phòng thành công!");
            return "redirect:/reception/rooms";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/reception/update/" + id + "/rooms";
        }
    }

}
