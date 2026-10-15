package com.example.parking.controller.web;

import com.example.parking.service.ApplicationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientPortalController {
    private final ApplicationService service;

    public ClientPortalController(ApplicationService service) {
        this.service = service;
    }

    @GetMapping("/client")
    public String dashboard(Authentication authentication, Model model) {
        var customer = service.getCustomerByEmail(authentication.getName());
        var page = PageRequest.of(0, 100);
        var filter = "customerId:" + customer.id();
        model.addAttribute("title", "Mon espace parking");
        model.addAttribute("customer", customer);
        model.addAttribute("vehicles", service.listVehicle("", "", filter, page).content());
        model.addAttribute("reservations", service.listReservation("", "", filter, page).content());
        model.addAttribute("stays", service.listParkingStay("", "", filter, page).content());
        return "client-dashboard";
    }
}
