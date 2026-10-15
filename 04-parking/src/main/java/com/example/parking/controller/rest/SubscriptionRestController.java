package com.example.parking.controller.rest;

import com.example.parking.service.ApplicationService;
import com.example.parking.dto.request.*;
import com.example.parking.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionRestController {
    private final ApplicationService service;
    public SubscriptionRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<SubscriptionResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listSubscription(q,status,filter,pageable); }
    @GetMapping("/{id}") public SubscriptionResponse get(@PathVariable Long id) { return service.getSubscription(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest r) { var result=service.createSubscription(r); return ResponseEntity.created(URI.create("/api/subscriptions/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SubscriptionResponse update(@PathVariable Long id, @Valid @RequestBody SubscriptionRequest r) { return service.updateSubscription(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteSubscription(id); }
}
