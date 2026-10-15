package com.example.taxis.controller.web;

import com.example.taxis.service.ApplicationService;
import com.example.taxis.dto.request.*;
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
        return "redirect:/drivers";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Véhicules"); m.addAttribute("path","/vehicles");

    }
    @GetMapping("/new") public String form(Model m) { return "redirect:/drivers/new"; }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model m) { return "redirect:/drivers"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getVehicle(id)); m.addAttribute("audit",service.history("Vehicle",id)); service.detail("Vehicle",id,m); return "vehicle-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") VehicleRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        flash.addFlashAttribute("error","Une voiture doit être enregistrée avec son chauffeur"); return "redirect:/drivers/new";
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") VehicleRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        flash.addFlashAttribute("error","Modifiez la voiture depuis la fiche du chauffeur"); return "redirect:/drivers";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id,RedirectAttributes flash) {flash.addFlashAttribute("error","La voiture appartient à un chauffeur");return "redirect:/drivers";}
}
