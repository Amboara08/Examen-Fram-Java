package com.example.textile.controller.web;

import com.example.textile.service.ApplicationService;
import com.example.textile.dto.request.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
@Controller
@RequestMapping("/order-lines")
public class OrderLineWebController {
    private final ApplicationService service;
    public OrderLineWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listOrderLine(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Lignes de commande"); m.addAttribute("path","/order-lines"); return "orderline-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Lignes de commande"); m.addAttribute("path","/order-lines");
        m.addAttribute("orderIdChoices",service.listTextileOrder("","","",PageRequest.of(0,100)).content());
        m.addAttribute("productIdChoices",service.listProduct("","","",PageRequest.of(0,100)).content());

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "orderline-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getOrderLine(id)); return "orderline-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getOrderLine(id)); m.addAttribute("audit",service.history("OrderLine",id)); service.detail("OrderLine",id,m); return "orderline-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") OrderLineRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "orderline-form"; }
        try { var saved=service.createOrderLine(r); flash.addFlashAttribute("success","Enregistrement créé"); return "redirect:/order-lines/"+saved.id(); }
        catch(com.example.textile.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "orderline-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") OrderLineRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "orderline-form"; }
        try {service.updateOrderLine(id,r); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/order-lines/"+id;}
        catch(com.example.textile.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "orderline-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteOrderLine(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/order-lines";}
}
