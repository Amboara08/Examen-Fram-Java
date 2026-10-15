package com.example.tournois.controller.rest;

import com.example.tournois.service.ApplicationService;
import com.example.tournois.dto.request.*;
import com.example.tournois.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;
@RestController
@RequestMapping("/api/registrations")
public class RegistrationRestController {
    private final ApplicationService service;
    public RegistrationRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<RegistrationResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listRegistration(q,status,filter,pageable); }
    @GetMapping("/{id}") public RegistrationResponse get(@PathVariable Long id) { return service.getRegistration(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<RegistrationResponse> create(@Valid @RequestBody RegistrationRequest r) { var result=service.createRegistration(r); return ResponseEntity.created(URI.create("/api/registrations/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public RegistrationResponse update(@PathVariable Long id, @Valid @RequestBody RegistrationRequest r) { return service.updateRegistration(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteRegistration(id); }
}
