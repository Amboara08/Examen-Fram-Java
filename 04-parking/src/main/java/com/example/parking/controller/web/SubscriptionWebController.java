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
@RequestMapping("/subscriptions")
public class SubscriptionWebController {
    private final ApplicationService service;
    public SubscriptionWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listSubscription(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Abonnements"); m.addAttribute("path","/subscriptions"); return "subscription-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Abonnements"); m.addAttribute("path","/subscriptions");
        m.addAttribute("customerIdChoices",service.listCustomer("","","",PageRequest.of(0,100)).content());
        m.addAttribute("vehicleIdChoices",service.listVehicle("","","",PageRequest.of(0,100)).content());

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "subscription-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getSubscription(id)); return "subscription-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getSubscription(id)); m.addAttribute("audit",service.history("Subscription",id)); service.detail("Subscription",id,m); return "subscription-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") SubscriptionRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "subscription-form"; }
        try { var saved=service.createSubscription(r); flash.addFlashAttribute("success","Enregistrement créé"); return "redirect:/subscriptions/"+saved.id(); }
        catch(com.example.parking.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "subscription-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") SubscriptionRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "subscription-form"; }
        try {service.updateSubscription(id,r); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/subscriptions/"+id;}
        catch(com.example.parking.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "subscription-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteSubscription(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/subscriptions";}
}
