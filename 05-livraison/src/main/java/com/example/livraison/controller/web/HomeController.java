package com.example.livraison.controller.web;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.livraison.service.ApplicationService;
import com.example.livraison.dto.request.*;
import com.example.livraison.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller public class HomeController {
 private final ApplicationService service;
 public HomeController(ApplicationService service){this.service=service;}
 @GetMapping("/login") public String login(){return "login";}
 @GetMapping("/") public String home(Model m){
  m.addAttribute("title","Accueil"); m.addAttribute("path","/");
  return "dashboard";
 }
 @GetMapping("/statistics") public String statistics(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(required=false) Long actorId,@RequestParam(defaultValue="") String zone,@RequestParam(defaultValue="") String period,Model m){if(!period.isBlank()){to=LocalDate.now();from=switch(period){case "day"->to;case "week"->to.with(java.time.DayOfWeek.MONDAY);case "month"->to.withDayOfMonth(1);default->from;};}m.addAttribute("title","Statistiques");m.addAttribute("path","/statistics");m.addAttribute("stats",service.statistics(from,to,actorId,zone));m.addAttribute("from",from);m.addAttribute("to",to);m.addAttribute("actorId",actorId);m.addAttribute("zone",zone);return "statistics";}
 @GetMapping("/settings") @PreAuthorize("hasRole('ADMIN')") public String settings(Model m){m.addAttribute("title","Paramètres");m.addAttribute("path","/settings");m.addAttribute("settings",service.settings());return "settings";}
 @PostMapping("/settings") @PreAuthorize("hasRole('ADMIN')") public String setting(@Valid @ModelAttribute SettingRequest r,RedirectAttributes flash){service.setting(r);flash.addFlashAttribute("success","Paramètre enregistré");return "redirect:/settings";}
 @PostMapping("/orders/{id}/actions/assign") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action0(@PathVariable Long id,@RequestParam Long delivererId,RedirectAttributes flash){try{service.assignOrder(id,delivererId);flash.addFlashAttribute("success","Livreur affecté");}catch(com.example.livraison.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/orders/"+id;}
 @PostMapping("/orders/{id}/actions/pickup") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action1(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("DeliveryOrder",id,"pickup",r);flash.addFlashAttribute("success","Prendre en charge : effectué");}catch(com.example.livraison.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/orders/"+id;}
 @PostMapping("/orders/{id}/actions/start") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action2(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("DeliveryOrder",id,"start",r);flash.addFlashAttribute("success","Démarrer : effectué");}catch(com.example.livraison.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/orders/"+id;}
 @PostMapping("/orders/{id}/actions/deliver") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action3(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("DeliveryOrder",id,"deliver",r);flash.addFlashAttribute("success","Confirmer la livraison : effectué");}catch(com.example.livraison.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/orders/"+id;}
 @PostMapping("/orders/{id}/actions/cancel") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action4(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("DeliveryOrder",id,"cancel",r);flash.addFlashAttribute("success","Annuler : effectué");}catch(com.example.livraison.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/orders/"+id;}
 @PostMapping("/orders/{id}/actions/return") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action5(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("DeliveryOrder",id,"return",r);flash.addFlashAttribute("success","Retourner : effectué");}catch(com.example.livraison.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/orders/"+id;}
 @PostMapping("/incidents/{id}/actions/resolve") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action6(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Incident",id,"resolve",r);flash.addFlashAttribute("success","Résoudre : effectué");}catch(com.example.livraison.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/incidents/"+id;}
 @PostMapping("/settings/zone") @PreAuthorize("hasRole('ADMIN')") public String zone(@RequestParam String zone,@RequestParam double value,RedirectAttributes flash){service.setting(new SettingRequest("price.zone."+zone,value));flash.addFlashAttribute("success","Tarif de zone enregistré");return "redirect:/settings";}
}
