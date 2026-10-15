package com.example.textile.controller.rest;

import com.example.textile.service.ApplicationService;
import com.example.textile.dto.request.*;
import com.example.textile.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;
@RestController
@RequestMapping("/api/production-steps")
public class ProductionStepRestController {
    private final ApplicationService service;
    public ProductionStepRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<ProductionStepResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listProductionStep(q,status,filter,pageable); }
    @GetMapping("/{id}") public ProductionStepResponse get(@PathVariable Long id) { return service.getProductionStep(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<ProductionStepResponse> create(@Valid @RequestBody ProductionStepRequest r) { var result=service.createProductionStep(r); return ResponseEntity.created(URI.create("/api/production-steps/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductionStepResponse update(@PathVariable Long id, @Valid @RequestBody ProductionStepRequest r) { return service.updateProductionStep(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteProductionStep(id); }
}
