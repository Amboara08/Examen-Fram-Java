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
@RequestMapping("/drivers")
public class DriverWebController {
    private final ApplicationService service;
    public DriverWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listDriver(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Chauffeurs"); m.addAttribute("path","/drivers"); return "driver-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Chauffeurs"); m.addAttribute("path","/drivers");
        m.addAttribute("vehicleIdChoices",service.listVehicle("","","",PageRequest.of(0,100)).content());
        m.addAttribute("generatedLicense","AUTO-"+java.util.UUID.randomUUID().toString().substring(0,8).toUpperCase());
        m.addAttribute("today",java.time.LocalDate.now());
        m.addAttribute("licenseExpiry",java.time.LocalDate.now().plusYears(20));
        m.addAttribute("currentYear",java.time.LocalDate.now().getYear());

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "driver-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getDriver(id));m.addAttribute("vehicle",service.getVehicleByDriver(id)); return "driver-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getDriver(id));m.addAttribute("vehicle",service.getVehicleByDriver(id)); m.addAttribute("audit",service.history("Driver",id)); service.detail("Driver",id,m); return "driver-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") DriverRequest r,BindingResult errors,@RequestParam String password,@RequestParam String plate,@RequestParam String brand,@RequestParam String carModel,@RequestParam Integer year,@RequestParam Integer capacity,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "driver-form"; }
        try { var saved=service.createDriverWithVehicle(r,password,new VehicleRequest(plate,brand,carModel,year,capacity,java.math.BigDecimal.ZERO,"DISPONIBLE")); flash.addFlashAttribute("success","Chauffeur et voiture créés"); return "redirect:/drivers/"+saved.id(); }
        catch(com.example.taxis.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "driver-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") DriverRequest r,BindingResult errors,@RequestParam(defaultValue="") String password,@RequestParam String plate,@RequestParam String brand,@RequestParam String carModel,@RequestParam Integer year,@RequestParam Integer capacity,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "driver-form"; }
        try {service.updateDriverWithVehicle(id,r,password,new VehicleRequest(plate,brand,carModel,year,capacity,java.math.BigDecimal.ZERO,"DISPONIBLE")); flash.addFlashAttribute("success","Chauffeur et voiture modifiés"); return "redirect:/drivers/"+id;}
        catch(com.example.taxis.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "driver-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteDriver(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/drivers";}
}
