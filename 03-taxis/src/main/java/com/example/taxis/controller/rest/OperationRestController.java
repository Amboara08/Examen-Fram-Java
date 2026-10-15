package com.example.taxis.controller.rest;

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
@RestController @RequestMapping("/api") public class OperationRestController {
 private final ApplicationService service;
 public OperationRestController(ApplicationService service){this.service=service;}
 @PostMapping("/rides/{id}/assign") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action0(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Ride",id,"assign",request==null?ActionRequest.empty():request);}
 @PostMapping("/rides/{id}/start") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action1(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Ride",id,"start",request==null?ActionRequest.empty():request);}
 @PostMapping("/rides/{id}/complete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action2(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Ride",id,"complete",request==null?ActionRequest.empty():request);}
 @PostMapping("/rides/{id}/cancel") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action3(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Ride",id,"cancel",request==null?ActionRequest.empty():request);}
 @GetMapping({"/statistics/revenue","/statistics/activity","/statistics/deliveries","/statistics"}) public AnalyticsResponse stats(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(required=false) Long actorId,@RequestParam(defaultValue="") String zone){return service.statistics(from,to,actorId,zone);}
 @GetMapping("/settings") @PreAuthorize("hasRole('ADMIN')") public List<SettingResponse> settings(){return service.settings();}
 @PutMapping("/settings") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void setting(@Valid @RequestBody SettingRequest r){service.setting(r);}
}
