package com.hotelmanage.controller.manager;

import com.hotelmanage.service.manager.ManagerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
@Slf4j
public class ManagerDashboardController {

    private final ManagerDashboardService managerDashboardService;

    @GetMapping
    public String dashboard(Model model) {
        log.info("Loading manager dashboard");
        managerDashboardService.getDashboardStats().forEach(model::addAttribute);
        return "manager/manager-dashboard";
    }
}
