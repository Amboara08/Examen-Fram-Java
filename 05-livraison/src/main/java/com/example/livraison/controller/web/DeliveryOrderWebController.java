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
@RequestMapping("/orders")
public class DeliveryOrderWebController {
    private final ApplicationService service;
    public DeliveryOrderWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listDeliveryOrder(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Commandes"); m.addAttribute("path","/orders"); return "deliveryorder-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Commandes"); m.addAttribute("path","/orders");
        m.addAttribute("clientIdChoices",service.listClient("","","",PageRequest.of(0,100)).content());
        m.addAttribute("delivererChoices",service.listDeliverer("","","",PageRequest.of(0,100)).content());

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { return "redirect:/orders"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { return "redirect:/orders/"+id; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getDeliveryOrder(id)); m.addAttribute("audit",service.history("DeliveryOrder",id)); service.detail("DeliveryOrder",id,m); return "deliveryorder-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") DeliveryOrderRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        flash.addFlashAttribute("error","Les commandes sont créées uniquement par les clients");return "redirect:/orders";
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") DeliveryOrderRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        flash.addFlashAttribute("error","Une commande client ne peut pas être modifiée");return "redirect:/orders/"+id;
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {flash.addFlashAttribute("error","Les commandes sont conservées dans l'historique");return "redirect:/orders/"+id;}
}
