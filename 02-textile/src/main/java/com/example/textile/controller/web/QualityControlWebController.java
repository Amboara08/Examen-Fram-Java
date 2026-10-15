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
@RequestMapping("/quality-controls")
public class QualityControlWebController {
    private final ApplicationService service;
    public QualityControlWebController(ApplicationService service) { this.service=service; }
    @GetMapping public String list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable,Model m) {
        prepare(m); m.addAttribute("page",service.listQualityControl(q,status,filter,pageable)); m.addAttribute("q",q); m.addAttribute("status",status); m.addAttribute("filter",filter); m.addAttribute("title","Contrôles qualité"); m.addAttribute("path","/quality-controls"); return "qualitycontrol-list";
    }
    private void prepare(Model m) {
        m.addAttribute("title","Contrôles qualité"); m.addAttribute("path","/quality-controls");
        m.addAttribute("orderIdChoices",service.listTextileOrder("","","",PageRequest.of(0,100)).content());

    }
    @GetMapping("/new") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String form(Model m) { prepare(m); m.addAttribute("editing",false); return "qualitycontrol-form"; }
    @GetMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String edit(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("record",service.getQualityControl(id)); return "qualitycontrol-form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model m) { prepare(m); m.addAttribute("record",service.getQualityControl(id)); m.addAttribute("audit",service.history("QualityControl",id)); service.detail("QualityControl",id,m); return "qualitycontrol-detail"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public String save(@Valid @ModelAttribute("record") QualityControlRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "qualitycontrol-form"; }
        try { var saved=service.createQualityControl(r); flash.addFlashAttribute("success","Enregistrement créé"); return "redirect:/quality-controls/"+saved.id(); }
        catch(com.example.textile.exception.BusinessException e) { prepare(m); m.addAttribute("editing",false); m.addAttribute("error",e.getMessage()); return "qualitycontrol-form"; }
    }
    @PostMapping("/{id}/edit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String update(@PathVariable Long id,@Valid @ModelAttribute("record") QualityControlRequest r,BindingResult errors,Model m,RedirectAttributes flash) {
        if(errors.hasErrors()) { prepare(m); m.addAttribute("editing",true); m.addAttribute("id",id); m.addAttribute("error",errors.getAllErrors().get(0).getDefaultMessage()); return "qualitycontrol-form"; }
        try {service.updateQualityControl(id,r); flash.addFlashAttribute("success","Modifications enregistrées"); return "redirect:/quality-controls/"+id;}
        catch(com.example.textile.exception.BusinessException e) {prepare(m); m.addAttribute("editing",true);m.addAttribute("id",id);m.addAttribute("error",e.getMessage());return "qualitycontrol-form";}
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String delete(@PathVariable Long id,RedirectAttributes flash) {service.deleteQualityControl(id);flash.addFlashAttribute("success","Enregistrement supprimé");return "redirect:/quality-controls";}
}
