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
@RequestMapping("/reservations")
public class ReservationWebController {
    private final ApplicationService service;
    public ReservationWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listReservation(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Réservations"); m.addAttribute("path","/reservations"); return "reservation-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Réservations"); m.addAttribute("path","/reservations");
        m.addAttribute("customerIdChoices",service.listCustomer("","","",PageRequest.of(0,100)).content());
        m.addAttribute("vehicleIdChoices",service.listVehicle("","","",PageRequest.of(0,100)).content());
        m.addAttribute("spaceIdChoices",service.listParkingSpace("","","",PageRequest.of(0,100)).content());

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "reservation-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getReservation(id)); return "reservation-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getReservation(id)); m.addAttribute("audit",service.history("Reservation",id)); service.detail("Reservation",id,m); return "reservation-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") ReservationRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "reservation-form"; }
        try { var saved=service.createReservation(r); flash.addFlashAttribute("success","Enregistrement créé"); return "redirect:/reservations/"+saved.id(); }
        catch(com.example.parking.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "reservation-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") ReservationRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "reservation-form"; }
        try {service.updateReservation(id,r); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/reservations/"+id;}
        catch(com.example.parking.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "reservation-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteReservation(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/reservations";}
}
