package com.example.livraison.controller.web;

import com.example.livraison.service.ApplicationService;
import com.example.livraison.dto.request.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
@Controller
@RequestMapping("/clients")
public class ClientWebController {
    private final ApplicationService service;
    public ClientWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listClient(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Clients"); m.addAttribute("path","/clients"); return "client-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Clients"); m.addAttribute("path","/clients");

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "client-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getClient(id)); return "client-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getClient(id)); m.addAttribute("audit",service.history("Client",id)); service.detail("Client",id,m); return "client-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") ClientRequest r,BindingResult errors,@RequestParam(defaultValue="") String password,Model m,RedirectAttributes flash) {
        if(password.length()<8) errors.reject("password","Le mot de passe doit contenir au moins 8 caractères");
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "client-form"; }
        try { var saved=service.createClient(r);service.setClientPassword(saved.id(),password); flash.addFlashAttribute("success","Compte client créé"); return "redirect:/clients/"+saved.id(); }
        catch(com.example.livraison.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "client-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") ClientRequest r,BindingResult errors,@RequestParam(defaultValue="") String password,Model m,RedirectAttributes flash) {
        if(!password.isBlank()&&password.length()<8) errors.reject("password","Le mot de passe doit contenir au moins 8 caractères");
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "client-form"; }
        try {service.updateClient(id,r);if(!password.isBlank())service.setClientPassword(id,password); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/clients/"+id;}
        catch(com.example.livraison.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "client-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteClient(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/clients";}
}
