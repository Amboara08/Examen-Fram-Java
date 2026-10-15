package com.example.parking.controller.rest;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.parking.service.ApplicationService;
import com.example.parking.dto.request.*;
import com.example.parking.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
@RestController @RequestMapping("/api") public class OperationRestController {
 private final ApplicationService service;
 public OperationRestController(ApplicationService service){this.service=service;}
 @PostMapping("/parking/stays/{id}/exit") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action0(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("ParkingStay",id,"exit",request==null?ActionRequest.empty():request);}
 @PostMapping("/reservations/{id}/cancel") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action1(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Reservation",id,"cancel",request==null?ActionRequest.empty():request);}
 @PostMapping("/subscriptions/{id}/cancel") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action2(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Subscription",id,"cancel",request==null?ActionRequest.empty():request);}
 @GetMapping({"/statistics/revenue","/statistics/activity","/statistics/deliveries","/statistics"}) public AnalyticsResponse stats(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(required=false) Long actorId,@RequestParam(defaultValue="") String zone){return service.statistics(from,to,actorId,zone);}
 @GetMapping("/settings") @PreAuthorize("hasRole('ADMIN')") public List<SettingResponse> settings(){return service.settings();}
 @PutMapping("/settings") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void setting(@Valid @RequestBody SettingRequest r){service.setting(r);}
 @PostMapping("/parking/entries") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public ResponseEntity<ParkingStayResponse> entry(@Valid @RequestBody ParkingStayRequest r){var s=service.createParkingStay(r);return ResponseEntity.created(java.net.URI.create("/api/parking/stays/"+s.id())).body(s);}
 @PostMapping("/parking/exits") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object exit(@Valid @RequestBody ActionRequest r){return service.perform("ParkingStay",r.stayId(),"exit",r);}
 @GetMapping("/parking/spaces/available") public PageResponse<ParkingSpaceResponse> available(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrival,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime departure,@RequestParam(required=false) Long customerId,@RequestParam(required=false) Long vehicleId,org.springframework.data.domain.Pageable pageable){return service.availableSpaces(arrival,departure,customerId,vehicleId,pageable);}
 @GetMapping({"/parking/occupancy","/parking/revenue"}) public AnalyticsResponse parkingStats(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(defaultValue="") String zone){return service.statistics(from,to,null,zone);}
}
