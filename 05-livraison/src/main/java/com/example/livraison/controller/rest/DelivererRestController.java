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
@RequestMapping("/api/deliverers")
public class DelivererRestController {
    private final ApplicationService service;
    public DelivererRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<DelivererResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listDeliverer(q,status,filter,pageable); }
    @GetMapping("/{id}") public DelivererResponse get(@PathVariable Long id) { return service.getDeliverer(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<DelivererResponse> create(@Valid @RequestBody DelivererRequest r) { var result=service.createDeliverer(r); return ResponseEntity.created(URI.create("/api/deliverers/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public DelivererResponse update(@PathVariable Long id, @Valid @RequestBody DelivererRequest r) { return service.updateDeliverer(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteDeliverer(id); }
}
