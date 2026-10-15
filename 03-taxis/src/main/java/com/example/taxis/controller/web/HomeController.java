package com.example.taxis.controller.web;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.taxis.service.ApplicationService;
import com.example.taxis.dto.request.*;
import com.example.taxis.dto.response.*;
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
  m.addAttribute("stats",service.statistics(null,null,null,""));
  return "dashboard";
 }
 @GetMapping("/statistics") public String statistics(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(required=false) Long actorId,@RequestParam(defaultValue="") String zone,@RequestParam(defaultValue="") String period,Model m){if(!period.isBlank()){to=LocalDate.now();from=switch(period){case "day"->to;case "week"->to.with(java.time.DayOfWeek.MONDAY);case "month"->to.withDayOfMonth(1);default->from;};}m.addAttribute("title","Statistiques");m.addAttribute("path","/statistics");m.addAttribute("stats",service.statistics(from,to,actorId,zone));m.addAttribute("from",from);m.addAttribute("to",to);m.addAttribute("actorId",actorId);m.addAttribute("zone",zone);return "statistics";}
 @GetMapping("/settings") @PreAuthorize("hasRole('ADMIN')") public String settings(Model m){m.addAttribute("title","Paramètres");m.addAttribute("path","/settings");m.addAttribute("settings",service.settings());return "settings";}
 @PostMapping("/settings") @PreAuthorize("hasRole('ADMIN')") public String setting(@Valid @ModelAttribute SettingRequest r,RedirectAttributes flash){service.setting(r);flash.addFlashAttribute("success","Paramètre enregistré");return "redirect:/settings";}
 @PostMapping("/rides/{id}/actions/assign") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action0(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Ride",id,"assign",r);flash.addFlashAttribute("success","Attribuer : effectué");}catch(com.example.taxis.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/rides/"+id;}
 @PostMapping("/rides/{id}/actions/start") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action1(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Ride",id,"start",r);flash.addFlashAttribute("success","Démarrer : effectué");}catch(com.example.taxis.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/rides/"+id;}
 @PostMapping("/rides/{id}/actions/complete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action2(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Ride",id,"complete",r);flash.addFlashAttribute("success","Terminer : effectué");}catch(com.example.taxis.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/rides/"+id;}
 @PostMapping("/rides/{id}/actions/cancel") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action3(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Ride",id,"cancel",r);flash.addFlashAttribute("success","Annuler : effectué");}catch(com.example.taxis.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/rides/"+id;}
}
