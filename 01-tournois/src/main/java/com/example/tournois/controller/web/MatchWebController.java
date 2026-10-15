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
@RequestMapping("/matches")
public class MatchWebController {
    private final ApplicationService service;
    public MatchWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listMatch(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Matchs"); m.addAttribute("path","/matches"); return "match-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Matchs"); m.addAttribute("path","/matches");
        m.addAttribute("tournamentIdChoices",service.listTournament("","","",PageRequest.of(0,100)).content());
        m.addAttribute("homeTeamIdChoices",service.listTeam("","","",PageRequest.of(0,100)).content());
        m.addAttribute("awayTeamIdChoices",service.listTeam("","","",PageRequest.of(0,100)).content());

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "match-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getMatch(id)); return "match-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getMatch(id)); m.addAttribute("audit",service.history("Match",id)); service.detail("Match",id,m); return "match-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") MatchRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "match-form"; }
        try { var saved=service.createMatch(r); flash.addFlashAttribute("success","Enregistrement créé"); return "redirect:/matches/"+saved.id(); }
        catch(com.example.tournois.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "match-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") MatchRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "match-form"; }
        try {service.updateMatch(id,r); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/matches/"+id;}
        catch(com.example.tournois.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "match-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteMatch(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/matches";}
}
