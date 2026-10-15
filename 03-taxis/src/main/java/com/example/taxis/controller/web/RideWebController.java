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
@RequestMapping("/rides")
public class RideWebController {
    private final ApplicationService service;
    public RideWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listRide(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Courses"); m.addAttribute("path","/rides"); return "ride-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Courses"); m.addAttribute("path","/rides");
        m.addAttribute("clientIdChoices",service.listClient("","","",PageRequest.of(0,100)).content());

    }
    @GetMapping("/new") public String form(Model m) { return "redirect:/"; }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model m) { return "redirect:/rides/"+id; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getRide(id)); m.addAttribute("audit",service.history("Ride",id)); service.detail("Ride",id,m); return "ride-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") RideRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        flash.addFlashAttribute("error","Les demandes de taxi sont créées par les clients"); return "redirect:/";
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") RideRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        flash.addFlashAttribute("error","Une demande doit être modifiée par son client avant sa prise en charge"); return "redirect:/rides/"+id;
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteRide(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/rides";}
}
