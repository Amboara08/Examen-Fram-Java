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
@RequestMapping("/users")
public class CustomerWebController {
    private final ApplicationService service;
    public CustomerWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listCustomer(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Usagers"); m.addAttribute("path","/users"); return "customer-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Usagers"); m.addAttribute("path","/users");

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "customer-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getCustomer(id)); addVehicle(id,m); return "customer-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getCustomer(id)); addVehicle(id,m); m.addAttribute("audit",service.history("Customer",id)); service.detail("Customer",id,m); return "customer-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") CustomerRequest r,BindingResult errors,@RequestParam(defaultValue="") String password,@RequestParam(defaultValue="") String plate,@RequestParam(defaultValue="") String vehicleType,@RequestParam(defaultValue="") String brand,@RequestParam(defaultValue="") String vehicleModel,Model m,RedirectAttributes flash) {
        if(password.length()<8) errors.reject("password","Le mot de passe doit contenir au moins 8 caractères");
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "customer-form"; }
        try { var saved=service.createCustomerWithVehicle(r,password,plate,vehicleType,brand,vehicleModel); flash.addFlashAttribute("success","Compte client et véhicule créés"); return "redirect:/users/"+saved.id(); }
        catch(com.example.parking.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "customer-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") CustomerRequest r,BindingResult errors,@RequestParam(defaultValue="") String password,@RequestParam(defaultValue="") String plate,@RequestParam(defaultValue="") String vehicleType,@RequestParam(defaultValue="") String brand,@RequestParam(defaultValue="") String vehicleModel,Model m,RedirectAttributes flash) {
        if(!password.isBlank() && password.length()<8) errors.reject("password","Le mot de passe doit contenir au moins 8 caractères");
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); addVehicle(id,m); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "customer-form"; }
        try {service.updateCustomerWithVehicle(id,r,password,plate,vehicleType,brand,vehicleModel);flash.addFlashAttribute("success","Client et véhicule modifiés"); return "redirect:/users/"+id;}
        catch(com.example.parking.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);addVehicle(id,m);m.addAttribute("error",e.getMessage());return "customer-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteCustomer(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/users";}
    private void addVehicle(Long customerId,Model model) {
        try { model.addAttribute("vehicle",service.getVehicleByCustomer(customerId)); }
        catch(com.example.parking.exception.ResourceNotFoundException ignored) { model.addAttribute("vehicle",null); }
    }
}
