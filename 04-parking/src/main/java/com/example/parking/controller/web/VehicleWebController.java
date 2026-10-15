package com.example.parking.controller.web;

import com.example.parking.service.ApplicationService;
import com.example.parking.dto.request.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
@Controller
@RequestMapping("/vehicles")
public class VehicleWebController {
    private final ApplicationService service;
    public VehicleWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        return "redirect:/users";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Véhicules"); m.addAttribute("path","/vehicles");
        m.addAttribute("customerIdChoices",service.listCustomer("","","",PageRequest.of(0,100)).content());

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { return "redirect:/users/new"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { return "redirect:/users"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { return "redirect:/users"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") VehicleRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        flash.addFlashAttribute("error","Ajoutez la voiture directement avec le compte client");
        return "redirect:/users/new";
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") VehicleRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        flash.addFlashAttribute("error","Modifiez la voiture depuis la fiche du client");
        return "redirect:/users";
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {flash.addFlashAttribute("error","La voiture est gérée depuis la fiche du client");return "redirect:/users";}
}
