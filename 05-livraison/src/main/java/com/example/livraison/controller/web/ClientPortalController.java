package com.example.livraison.controller.web;

import com.example.livraison.dto.request.ActionRequest;
import com.example.livraison.exception.BusinessException;
import com.example.livraison.service.ApplicationService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/client")
public class ClientPortalController {
    private final ApplicationService service;
    public ClientPortalController(ApplicationService service){this.service=service;}

    @GetMapping
    public String dashboard(Authentication authentication,Model model){
        var client=service.getClientByEmail(authentication.getName());
        model.addAttribute("title","Mon espace livraison");
        model.addAttribute("client",client);
        model.addAttribute("orders",service.listDeliveryOrder("","","clientId:"+client.id(),PageRequest.of(0,100)).content());
        return "client-dashboard";
    }

    @GetMapping("/orders/new")
    public String orderForm(Authentication authentication,Model model){
        model.addAttribute("title","Commander une livraison");
        model.addAttribute("client",service.getClientByEmail(authentication.getName()));
        return "client-order-form";
    }

    @PostMapping("/orders")
    public String createOrder(Authentication authentication,
        @RequestParam String pickupAddress,@RequestParam String deliveryAddress,
        @RequestParam BigDecimal weight,@RequestParam String priority,
        @RequestParam String recipientName,@RequestParam String recipientPhone,@RequestParam String description,
        RedirectAttributes flash){
        var client=service.getClientByEmail(authentication.getName());
        LocalDateTime expectedAt=LocalDateTime.now().plusHours(switch(priority){case "TRES_URGENTE"->6;case "URGENTE"->24;default->48;});
        try {
            String parcelDescription="Destinataire : "+recipientName+" · Téléphone : "+recipientPhone+" · "+description;
            var order=service.createClientOrder(client.id(),pickupAddress,deliveryAddress,weight,BigDecimal.ONE,priority,BigDecimal.ONE,"GENERAL",expectedAt,parcelDescription);
            flash.addFlashAttribute("success","Commande créée. Numéro de suivi : "+order.number());
            return "redirect:/client";
        } catch(BusinessException e){
            flash.addFlashAttribute("error",e.getMessage());
            return "redirect:/client/orders/new";
        }
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(Authentication authentication,@PathVariable Long id,RedirectAttributes flash){
        var client=service.getClientByEmail(authentication.getName());
        var order=service.getDeliveryOrder(id);
        if(!client.id().equals(order.clientId())) {flash.addFlashAttribute("error","Commande inaccessible");return "redirect:/client";}
        if(!"CREEE".equals(order.status())) {flash.addFlashAttribute("error","Annulation impossible après l'acceptation par un livreur");return "redirect:/client";}
        try {service.perform("DeliveryOrder",id,"cancel",ActionRequest.empty());flash.addFlashAttribute("success","Commande annulée");}
        catch(BusinessException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/client";
    }
}
