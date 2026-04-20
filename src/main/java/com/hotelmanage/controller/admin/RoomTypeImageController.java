package com.hotelmanage.controller.admin;

import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.entity.room.RoomTypeImage;
import com.hotelmanage.service.room.RoomTypeImageService;
import com.hotelmanage.service.room.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/room-types")
@RequiredArgsConstructor
public class RoomTypeImageController {

    private final RoomTypeService roomTypeService;
    private final RoomTypeImageService roomTypeImageService;

    @GetMapping("/{roomTypeId}/images")
    public String manageImages(@PathVariable Integer roomTypeId, Model model) {
        RoomType roomType = roomTypeService.findById(roomTypeId);
        model.addAttribute("roomType", roomType);
        model.addAttribute("images", roomType.getImages());
        return "admin/room-type-images";
    }

    @PostMapping("/{roomTypeId}/images/upload")
    public String uploadImages(@PathVariable Integer roomTypeId,
                               @RequestParam("files") MultipartFile[] files,
                               @RequestParam(required = false) boolean setPrimary,
                               RedirectAttributes redirectAttributes) {
        try {
            RoomType roomType = roomTypeService.findById(roomTypeId);
            List<RoomTypeImage> uploadedImages = roomTypeImageService.uploadImages(roomType, files, setPrimary);

            redirectAttributes.addFlashAttribute("success",
                    "Đã upload thành công " + uploadedImages.size() + " ảnh!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi upload ảnh: " + e.getMessage());
        }

        return "redirect:/admin/room-types/" + roomTypeId + "/images";
    }

    @PostMapping("/{roomTypeId}/images/{imageId}/set-primary")
    public String setPrimaryImage(@PathVariable Integer roomTypeId,
                                  @PathVariable Integer imageId,
                                  RedirectAttributes redirectAttributes) {
        try {
            roomTypeImageService.setPrimaryImage(imageId);
            redirectAttributes.addFlashAttribute("success", "Đã đặt làm ảnh chính!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/room-types/" + roomTypeId + "/images";
    }

    @PostMapping("/{roomTypeId}/images/{imageId}/delete")
    public String deleteImage(@PathVariable Integer roomTypeId,
                              @PathVariable Integer imageId,
                              RedirectAttributes redirectAttributes) {
        try {
            roomTypeImageService.deleteImage(imageId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa ảnh!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/room-types/" + roomTypeId + "/images";
    }
}
