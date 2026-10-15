package com.example.tournois.controller.web;

import com.example.tournois.service.ApplicationService;
import com.example.tournois.dto.request.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
@Controller
@RequestMapping("/registrations")
public class RegistrationWebController {
    private final ApplicationService service;
    public RegistrationWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listRegistration(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Inscriptions"); m.addAttribute("path","/registrations"); return "registration-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Inscriptions"); m.addAttribute("path","/registrations");
        m.addAttribute("tournamentIdChoices",service.listTournament("","","",PageRequest.of(0,100)).content());
        m.addAttribute("teamIdChoices",service.listTeam("","","",PageRequest.of(0,100)).content());

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "registration-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getRegistration(id)); return "registration-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getRegistration(id)); m.addAttribute("audit",service.history("Registration",id)); service.detail("Registration",id,m); return "registration-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") RegistrationRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "registration-form"; }
        try { var saved=service.createRegistration(r); flash.addFlashAttribute("success","Enregistrement créé"); return "redirect:/registrations/"+saved.id(); }
        catch(com.example.tournois.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "registration-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") RegistrationRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "registration-form"; }
        try {service.updateRegistration(id,r); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/registrations/"+id;}
        catch(com.example.tournois.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "registration-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteRegistration(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/registrations";}
}
