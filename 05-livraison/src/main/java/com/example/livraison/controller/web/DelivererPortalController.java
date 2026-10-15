package com.example.livraison.controller.web;

import com.example.livraison.dto.request.ActionRequest;
import com.example.livraison.exception.BusinessException;
import com.example.livraison.service.ApplicationService;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/livreur")
public class DelivererPortalController {
 private final ApplicationService service;
 public DelivererPortalController(ApplicationService service){this.service=service;}
 @GetMapping public String dashboard(Authentication auth,Model m){var d=service.getDelivererByEmail(auth.getName());m.addAttribute("title","Espace livreur");m.addAttribute("deliverer",d);m.addAttribute("available",service.listDeliveryOrder("","CREEE","",PageRequest.of(0,100)).content());m.addAttribute("mine",service.listDeliveryOrder("","","delivererId:"+d.id(),PageRequest.of(0,100)).content());m.addAttribute("clients",service.listClient("","","",PageRequest.of(0,100)).content());return "deliverer-dashboard";}
 @PostMapping("/orders/{id}/accept") public String accept(Authentication auth,@PathVariable Long id,RedirectAttributes f){try{var d=service.getDelivererByEmail(auth.getName());service.assignOrder(id,d.id());f.addFlashAttribute("success","Livraison acceptée");}catch(BusinessException e){f.addFlashAttribute("error",e.getMessage());}return "redirect:/livreur";}
 @PostMapping("/orders/{id}/{action}") public String action(Authentication auth,@PathVariable Long id,@PathVariable String action,@RequestParam(required=false) String recipient,@RequestParam(required=false) String confirmationCode,@RequestParam(required=false) String comment,RedirectAttributes f){var d=service.getDelivererByEmail(auth.getName());var o=service.getDeliveryOrder(id);if(!d.id().equals(o.delivererId())){f.addFlashAttribute("error","Livraison inaccessible");return "redirect:/livreur";}try{var r=new ActionRequest(null,null,null,null,null,null,comment,recipient,confirmationCode,"deliver".equals(action)?LocalDateTime.now():null,null,null);service.perform("DeliveryOrder",id,action,r);f.addFlashAttribute("success","Statut mis à jour");}catch(BusinessException e){f.addFlashAttribute("error",e.getMessage());}return "redirect:/livreur";}
}
