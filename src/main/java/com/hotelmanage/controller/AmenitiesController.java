package com.hotelmanage.controller;

import com.hotelmanage.entity.amenity.Amenity;
import com.hotelmanage.service.amenity.AmenityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
@Slf4j
@Controller
@RequiredArgsConstructor
public class AmenitiesController {
    private final AmenityService amenityService;

    @GetMapping("/amenities")
    public String viewAmenities(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Page<Amenity> amenityPage = amenityService.getAmenities(page, 3);

        model.addAttribute("amenities", amenityPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", amenityPage.getTotalPages());
        return "amenities/list";
    }

    @GetMapping("/amenities/{id}")
    public String showDetail(@PathVariable Integer id, Model model) {
        Amenity amenity = amenityService.findById(id);
        model.addAttribute("amenity", amenity);
        return "amenities/detail";
    }
}
