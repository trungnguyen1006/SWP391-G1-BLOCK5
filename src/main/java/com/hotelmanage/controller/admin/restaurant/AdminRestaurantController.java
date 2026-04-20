package com.hotelmanage.controller.admin.restaurant;




import com.hotelmanage.entity.restaurant.Restaurant;
import com.hotelmanage.repository.restaurant.RestaurantRepository;
import com.hotelmanage.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Restaurant> restaurants = (q == null || q.isBlank())
                ? restaurantRepository.findAll(pageable)
                : restaurantRepository.findByNameContainingIgnoreCase(q, pageable);
        model.addAttribute("page", restaurants);
        model.addAttribute("q", q);
        return "admin/restaurant/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("restaurant", new Restaurant());
        return "admin/restaurant/form";
    }

    @PostMapping
    public String create(@ModelAttribute Restaurant r,
                         @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                String url = cloudinaryService.uploadImage(image, "restaurants");
                r.setImageUrl(url);
            }
        } catch (Exception ignored) {}
        restaurantRepository.save(r);
        return "redirect:/admin/restaurants";
    }

    @GetMapping("/{id}/view")
    public String view(@PathVariable Long id, Model model) {
        Optional<Restaurant> r = restaurantRepository.findById(id);
        model.addAttribute("restaurant", r.orElse(null));
        return "admin/restaurant/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<Restaurant> r = restaurantRepository.findById(id);
        model.addAttribute("restaurant", r.orElseGet(Restaurant::new));
        return "admin/restaurant/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute Restaurant form,
                         @RequestParam(value = "image", required = false) MultipartFile image) {
        return restaurantRepository.findById(id).map(r -> {
            r.setName(form.getName());
            r.setCuisineType(form.getCuisineType());
            r.setDescription(form.getDescription());
            r.setOpeningHours(form.getOpeningHours());
            r.setContactInfo(form.getContactInfo());
            r.setPriceRange(form.getPriceRange());
            r.setPromotionText(form.getPromotionText());
            try {
                if (image != null && !image.isEmpty()) {
                    String url = cloudinaryService.uploadImage(image, "restaurants");
                    if (url != null) r.setImageUrl(url);
                }
            } catch (Exception ignored) {}
            restaurantRepository.save(r);
            return "redirect:/admin/restaurants";
        }).orElse("redirect:/admin/restaurants");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        restaurantRepository.deleteById(id);
        return "redirect:/admin/restaurants";
    }
}



