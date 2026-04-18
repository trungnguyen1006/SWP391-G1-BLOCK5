package com.hotelmanage.controller;

import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.service.room.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final RoomTypeService roomTypeService;

    @GetMapping("/")
    public String home(Model model) {

        List<RoomType> roomTypes = roomTypeService.findAll();


        model.addAttribute("roomTypes", roomTypes);

        return "users/homepage";
    }

    @GetMapping({"/admin", "/reception", "/home"})
    public String roleLanding(Authentication authentication) {
        if (authentication == null) return "index";
        for (GrantedAuthority a : authentication.getAuthorities()) {
            String role = a.getAuthority();
            if ("ROLE_ADMIN".equals(role)) return "admin/admin-dashboard";
            if ("ROLE_RECEPTIONIST".equals(role)) return "receptionist/receptionist-dashboard";
            if ("ROLE_CUSTOMER".equals(role)) return "redirect:/";
        }

        return "index";
    }
}