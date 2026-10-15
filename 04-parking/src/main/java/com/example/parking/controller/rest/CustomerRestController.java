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
@RequestMapping("/api/users")
public class CustomerRestController {
    private final ApplicationService service;
    public CustomerRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<CustomerResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listCustomer(q,status,filter,pageable); }
    @GetMapping("/{id}") public CustomerResponse get(@PathVariable Long id) { return service.getCustomer(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest r) { var result=service.createCustomer(r); return ResponseEntity.created(URI.create("/api/users/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest r) { return service.updateCustomer(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteCustomer(id); }
}
