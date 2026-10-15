package com.example.tournois.controller.web;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.tournois.service.ApplicationService;
import com.example.tournois.dto.request.*;
import com.example.tournois.dto.response.*;
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
 @PostMapping("/tournaments/{id}/actions/generate-matches") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action0(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Tournament",id,"generate-matches",r);flash.addFlashAttribute("success","Générer le calendrier : effectué");}catch(com.example.tournois.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/tournaments/"+id;}
 @PostMapping("/tournaments/{id}/actions/status") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action1(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Tournament",id,"status",r);flash.addFlashAttribute("success","Changer le statut : effectué");}catch(com.example.tournois.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/tournaments/"+id;}
 @PostMapping("/tournaments/{id}/actions/teams") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action2(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Tournament",id,"teams",r);flash.addFlashAttribute("success","Inscrire une équipe : effectué");}catch(com.example.tournois.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/tournaments/"+id;}
 @PostMapping("/matches/{id}/actions/result") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String action3(@PathVariable Long id,@Valid @ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Match",id,"result",r);flash.addFlashAttribute("success","Enregistrer le résultat : effectué");}catch(com.example.tournois.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/matches/"+id;}
 @PostMapping("/tournaments/{id}/actions/remove-team") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String remove(@PathVariable Long id,@ModelAttribute ActionRequest r,RedirectAttributes flash){try{service.perform("Tournament",id,"remove-team",r);flash.addFlashAttribute("success","Équipe retirée");}catch(com.example.tournois.exception.BusinessException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/tournaments/"+id;}
}
