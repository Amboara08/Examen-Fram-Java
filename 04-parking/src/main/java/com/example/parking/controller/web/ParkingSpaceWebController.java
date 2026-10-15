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
@RequestMapping("/parking/spaces")
public class ParkingSpaceWebController {
    private final ApplicationService service;
    public ParkingSpaceWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listParkingSpace(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Places"); m.addAttribute("path","/parking/spaces"); return "parkingspace-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Places"); m.addAttribute("path","/parking/spaces");

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "parkingspace-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getParkingSpace(id)); return "parkingspace-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getParkingSpace(id)); m.addAttribute("audit",service.history("ParkingSpace",id)); service.detail("ParkingSpace",id,m); return "parkingspace-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") ParkingSpaceRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "parkingspace-form"; }
        try { var saved=service.createParkingSpace(r); flash.addFlashAttribute("success","Enregistrement créé"); return "redirect:/parking/spaces/"+saved.id(); }
        catch(com.example.parking.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "parkingspace-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") ParkingSpaceRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "parkingspace-form"; }
        try {service.updateParkingSpace(id,r); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/parking/spaces/"+id;}
        catch(com.example.parking.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "parkingspace-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteParkingSpace(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/parking/spaces";}
}
