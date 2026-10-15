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
@RequestMapping("/teams")
public class TeamWebController {
    private final ApplicationService service;
    public TeamWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listTeam(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Équipes"); m.addAttribute("path","/teams"); return "team-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Équipes"); m.addAttribute("path","/teams");

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "team-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getTeam(id)); return "team-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getTeam(id)); m.addAttribute("players",service.listPlayers(id)); m.addAttribute("audit",service.history("Team",id)); service.detail("Team",id,m); return "team-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") TeamRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "team-form"; }
        try { var saved=service.createTeam(r); flash.addFlashAttribute("success","Enregistrement créé"); return "redirect:/teams/"+saved.id(); }
        catch(com.example.tournois.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "team-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") TeamRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "team-form"; }
        try {service.updateTeam(id,r); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/teams/"+id;}
        catch(com.example.tournois.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "team-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteTeam(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/teams";}
    @PostMapping("/{id}/players") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String addPlayer(@PathVariable Long id,@Valid @ModelAttribute PlayerRequest request,BindingResult errors,RedirectAttributes flash) {
        if(errors.hasErrors()) flash.addFlashAttribute("error","Vérifie le numéro, le nom, la taille et le poste du joueur");
        else try { service.addPlayer(id,request); flash.addFlashAttribute("success","Joueur ajouté"); }
        catch(com.example.tournois.exception.BusinessException e) { flash.addFlashAttribute("error",e.getMessage()); }
        return "redirect:/teams/"+id;
    }
    @PostMapping("/{teamId}/players/{playerId}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String deletePlayer(@PathVariable Long teamId,@PathVariable Long playerId,RedirectAttributes flash) {
        service.deletePlayer(teamId,playerId); flash.addFlashAttribute("success","Joueur supprimé"); return "redirect:/teams/"+teamId;
    }
}
