package com.hotelmanage.controller.restaurant;


import com.hotelmanage.repository.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    @GetMapping("/restaurants")
    public String listRestaurants(Model model) {
        model.addAttribute("restaurants", restaurantRepository.findAllByOrderByNameAsc());
        return "restaurant/list";
    }

    @GetMapping("/restaurants/{id}")
    public String restaurantDetail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("restaurant", restaurantRepository.findById(id).orElse(null));
        return "restaurant/detail";
    }
}
