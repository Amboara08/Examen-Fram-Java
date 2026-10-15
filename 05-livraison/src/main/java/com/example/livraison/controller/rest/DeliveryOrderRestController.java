package com.example.livraison.controller.rest;

import com.example.livraison.service.ApplicationService;
import com.example.livraison.dto.request.*;
import com.example.livraison.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;
@RestController
@RequestMapping("/api/orders")
public class DeliveryOrderRestController {
    private final ApplicationService service;
    public DeliveryOrderRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<DeliveryOrderResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listDeliveryOrder(q,status,filter,pageable); }
    @GetMapping("/{id}") public DeliveryOrderResponse get(@PathVariable Long id) { return service.getDeliveryOrder(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<DeliveryOrderResponse> create(@Valid @RequestBody DeliveryOrderRequest r) { var result=service.createDeliveryOrder(r); return ResponseEntity.created(URI.create("/api/orders/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public DeliveryOrderResponse update(@PathVariable Long id, @Valid @RequestBody DeliveryOrderRequest r) { return service.updateDeliveryOrder(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteDeliveryOrder(id); }
}
