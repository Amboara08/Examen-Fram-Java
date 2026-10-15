package com.example.textile.controller.rest;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.textile.service.ApplicationService;
import com.example.textile.dto.request.*;
import com.example.textile.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
@RestController @RequestMapping("/api") public class OperationRestController {
 private final ApplicationService service;
 public OperationRestController(ApplicationService service){this.service=service;}
 @PostMapping("/orders/{id}/production/start") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action0(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("TextileOrder",id,"production/start",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/production/steps/COUPE/complete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action1(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("TextileOrder",id,"COUPE",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/production/steps/COUTURE/complete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action2(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("TextileOrder",id,"COUTURE",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/production/steps/FINITION/complete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action3(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("TextileOrder",id,"FINITION",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/quality-control") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action4(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("TextileOrder",id,"quality-control",request==null?ActionRequest.empty():request);}
 @PostMapping("/orders/{id}/delivery") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action5(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("TextileOrder",id,"delivery",request==null?ActionRequest.empty():request);}
 @GetMapping({"/statistics/revenue","/statistics/activity","/statistics/deliveries","/statistics"}) public AnalyticsResponse stats(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(required=false) Long actorId,@RequestParam(defaultValue="") String zone){return service.statistics(from,to,actorId,zone);}
 @GetMapping("/settings") @PreAuthorize("hasRole('ADMIN')") public List<SettingResponse> settings(){return service.settings();}
 @PutMapping("/settings") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void setting(@Valid @RequestBody SettingRequest r){service.setting(r);}
 @GetMapping("/orders/{id}/production") public PageResponse<ProductionStepResponse> production(@PathVariable Long id,org.springframework.data.domain.Pageable pageable){service.getTextileOrder(id);return service.listProductionStep("","","orderId:"+id,pageable);}
 @GetMapping("/orders/delayed") public PageResponse<TextileOrderResponse> delayed(org.springframework.data.domain.Pageable pageable){return service.delayed(pageable);}
}
