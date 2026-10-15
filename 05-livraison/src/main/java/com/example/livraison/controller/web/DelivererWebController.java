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
@RequestMapping("/deliverers")
public class DelivererWebController {
    private final ApplicationService service;
    public DelivererWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listDeliverer(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Livreurs"); m.addAttribute("path","/deliverers"); return "deliverer-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Livreurs"); m.addAttribute("path","/deliverers");

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "deliverer-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getDeliverer(id)); return "deliverer-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getDeliverer(id)); m.addAttribute("audit",service.history("Deliverer",id)); service.detail("Deliverer",id,m); return "deliverer-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") DelivererRequest r,BindingResult errors,@RequestParam(defaultValue="") String password,Model m,RedirectAttributes flash) {
        if(password.length()<8)errors.reject("password","Le mot de passe doit contenir au moins 8 caractères");
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "deliverer-form"; }
        try { var saved=service.createDeliverer(r);service.setDelivererPassword(saved.id(),password); flash.addFlashAttribute("success","Compte livreur créé"); return "redirect:/deliverers/"+saved.id(); }
        catch(com.example.livraison.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "deliverer-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") DelivererRequest r,BindingResult errors,@RequestParam(defaultValue="") String password,Model m,RedirectAttributes flash) {
        if(!password.isBlank()&&password.length()<8)errors.reject("password","Le mot de passe doit contenir au moins 8 caractères");
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "deliverer-form"; }
        try {service.updateDeliverer(id,r);if(!password.isBlank())service.setDelivererPassword(id,password); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/deliverers/"+id;}
        catch(com.example.livraison.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "deliverer-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteDeliverer(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/deliverers";}
}
