package com.example.livraison.controller.rest;

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
@RestController @RequestMapping("/api") public class OperationRestController {
 private final ApplicationService service;
 public OperationRestController(ApplicationService service){this.service=service;}
 @PostMapping("/orders/{id}/assign") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action0(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("DeliveryOrder",id,"assign",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/pickup") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action1(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("DeliveryOrder",id,"pickup",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/start") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action2(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("DeliveryOrder",id,"start",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/deliver") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action3(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("DeliveryOrder",id,"deliver",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/cancel") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action4(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("DeliveryOrder",id,"cancel",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/return") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action5(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("DeliveryOrder",id,"return",request==null?ActionRequest.empty():request);}
 @PostMapping("/incidents/{id}/resolve") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action6(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Incident",id,"resolve",request==null?ActionRequest.empty():request);}
 @GetMapping({"/statistics/revenue","/statistics/activity","/statistics/deliveries","/statistics"}) public AnalyticsResponse stats(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(required=false) Long actorId,@RequestParam(defaultValue="") String zone){return service.statistics(from,to,actorId,zone);}
 @GetMapping("/settings") @PreAuthorize("hasRole('ADMIN')") public List<SettingResponse> settings(){return service.settings();}
 @PutMapping("/settings") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void setting(@Valid @RequestBody SettingRequest r){service.setting(r);}
 @GetMapping("/deliverers/available") public PageResponse<DelivererResponse> available(org.springframework.data.domain.Pageable pageable){return service.listDeliverer("","","",pageable);}
 @GetMapping("/orders/{id}/tracking") public List<AuditResponse> tracking(@PathVariable Long id){service.getDeliveryOrder(id);return service.history("DeliveryOrder",id);}
 @PostMapping("/orders/{id}/incidents") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public ResponseEntity<IncidentResponse> incident(@PathVariable Long id,@Valid @RequestBody IncidentRequest r){if(!id.equals(r.orderId()))throw new com.example.livraison.exception.InvalidOperationException("Commande incompatible");var result=service.createIncident(r);return ResponseEntity.created(java.net.URI.create("/api/incidents/"+result.id())).body(result);}
}
