package com.hotelmanage.controller.admin;

import com.hotelmanage.entity.room.Room;
import com.hotelmanage.service.room.RoomService;
import com.hotelmanage.service.room.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;

    /** Danh sách phòng */
    @GetMapping
    public String listRooms(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int size,
                            Model model) {
        Page<Room> roomPage = roomService.findPaginated(page, size);
        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomPage.getTotalPages());
        return "admin/room-list";
    }

    /** Tìm kiếm phòng */
    @GetMapping("/search")
    public String searchRooms(@RequestParam("keyword") String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "5") int size,
                              Model model) {
        Page<Room> roomPage = roomService.searchRooms(keyword, page, size);
        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        return "admin/room-list";
    }

    /** Hiển thị form thêm mới phòng */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", roomTypeService.findAll());
        return "admin/room-form";
    }

    /** Xử lý thêm mới phòng */
    @PostMapping("/create")
    public String createRoom(@Valid @ModelAttribute("room") Room room,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roomTypes", roomTypeService.findAll());
            return "admin/room-form";
        }
        try {
            roomService.save(room);
            redirectAttributes.addFlashAttribute("success", "Thêm phòng thành công!");
            return "redirect:/admin/rooms";
        } catch (Exception e) {
            log.error("Error creating room: {}", e.getMessage());
            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/room-form";
        }
    }

    /** Hiển thị form chỉnh sửa phòng */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Room room = roomService.findById(id);
            model.addAttribute("room", room);
            model.addAttribute("roomTypes", roomTypeService.findAll());
            return "admin/room-form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/rooms";
        }
    }

    /** Cập nhật thông tin phòng */
    @PostMapping("/update/{id}")
    public String updateRoom(@PathVariable Integer id,
                             @Valid @ModelAttribute("room") Room room,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roomTypes", roomTypeService.findAll());
            return "admin/room-form";
        }
        try {
            room.setRoomId(id);
            roomService.update(room);
            redirectAttributes.addFlashAttribute("success", "Cập nhật phòng thành công!");
            return "redirect:/admin/rooms";
        } catch (Exception e) {
            log.error("Error updating room: {}", e.getMessage());
            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/room-form";
        }
    }

    /** Xóa mềm phòng (đánh dấu deleted_at) */
    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Integer id,
                             RedirectAttributes redirectAttributes) {
        try {
            roomService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa phòng thành công (đã đánh dấu deleted_at).");
        } catch (RuntimeException e) {
            log.error("Error deleting room: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/rooms";
    }
}
