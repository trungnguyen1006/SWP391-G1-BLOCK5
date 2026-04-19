package com.hotelmanage.controller.admin;

import java.util.Map;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hotelmanage.entity.amenity.Amenity;
import com.hotelmanage.entity.room.Room;
import com.hotelmanage.service.amenity.AmenityService;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller("adminAmenitiesController")
@RequestMapping("/admin/amenities")
@RequiredArgsConstructor
public class AmenitiesController {
    private final AmenityService amenityService;
    private final Cloudinary cloudinary;

    @GetMapping
    public String listAmenities(Model model) {
        List<Amenity> amenities = amenityService.findAll();
        model.addAttribute("amenities", amenities);
        return "admin/amenity/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("amenity", new Amenity());
        return "admin/amenity/form";
    }

    @PostMapping("/create")
    public String createAmenity(@Valid @ModelAttribute("amenity") Amenity amenity,
                             BindingResult result, @RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            if (!file.isEmpty()) {
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("File phải là ảnh!");
                }

                // Upload ảnh lên Cloudinary
                Map uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "hotel/amenities",
                                "resource_type", "image"
                        )
                );

                String imageUrl = (String) uploadResult.get("secure_url");
                amenity.setImageUrl(imageUrl);
                //save to db
                amenityService.save(amenity);
                redirectAttributes.addFlashAttribute("success", "Thêm tiện nghi thành công!");
                return "redirect:/admin/amenities";
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Lỗi khi upload ảnh");
                return "redirect:/admin/amenities/create";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/amenities/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Amenity amenity = amenityService.findById(id);
        model.addAttribute("amenity", amenity);
        return "admin/amenity/form";
    }

    @PostMapping("/update/{id}")
    public String updateAmenity(@PathVariable Integer id,
                             @Valid @ModelAttribute("amenity") Amenity amenity,
                             BindingResult result, @RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            if (!file.isEmpty()) {
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("File phải là ảnh!");
                }

                // Upload ảnh lên Cloudinary
                Map uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "hotel/amenities",
                                "resource_type", "image"
                        )
                );

                String imageUrl = (String) uploadResult.get("secure_url");
                amenity.setImageUrl(imageUrl);
                //save to db
                amenity.setAmenityId(id);
                amenityService.update(amenity);
                redirectAttributes.addFlashAttribute("success", "Cập nhật tiện nghi thành công!");
                return "redirect:/admin/amenities";
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Lỗi khi upload ảnh");
                return "redirect:/admin/amenities/create";
            }


        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/amenities/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteAmenity(@PathVariable Integer id,
                             RedirectAttributes redirectAttributes) {
        try {
            amenityService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa tiện nghi thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/amenities";
    }
}
