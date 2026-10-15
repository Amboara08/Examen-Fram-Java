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
@RequestMapping("/api/products")
public class ProductRestController {
    private final ApplicationService service;
    public ProductRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<ProductResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listProduct(q,status,filter,pageable); }
    @GetMapping("/{id}") public ProductResponse get(@PathVariable Long id) { return service.getProduct(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest r) { var result=service.createProduct(r); return ResponseEntity.created(URI.create("/api/products/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest r) { return service.updateProduct(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteProduct(id); }
}
