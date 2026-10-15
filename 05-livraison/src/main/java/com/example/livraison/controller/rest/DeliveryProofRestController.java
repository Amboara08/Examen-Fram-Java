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
@RequestMapping("/api/proofs")
public class DeliveryProofRestController {
    private final ApplicationService service;
    public DeliveryProofRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<DeliveryProofResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listDeliveryProof(q,status,filter,pageable); }
    @GetMapping("/{id}") public DeliveryProofResponse get(@PathVariable Long id) { return service.getDeliveryProof(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<DeliveryProofResponse> create(@Valid @RequestBody DeliveryProofRequest r) { var result=service.createDeliveryProof(r); return ResponseEntity.created(URI.create("/api/proofs/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public DeliveryProofResponse update(@PathVariable Long id, @Valid @RequestBody DeliveryProofRequest r) { return service.updateDeliveryProof(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteDeliveryProof(id); }
}
