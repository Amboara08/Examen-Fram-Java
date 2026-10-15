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
@RequestMapping("/api/clients")
public class ClientRestController {
    private final ApplicationService service;
    public ClientRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<ClientResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listClient(q,status,filter,pageable); }
    @GetMapping("/{id}") public ClientResponse get(@PathVariable Long id) { return service.getClient(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest r) { var result=service.createClient(r); return ResponseEntity.created(URI.create("/api/clients/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientRequest r) { return service.updateClient(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteClient(id); }
}
