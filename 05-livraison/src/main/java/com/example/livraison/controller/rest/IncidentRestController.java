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
@RequestMapping("/api/incidents")
public class IncidentRestController {
    private final ApplicationService service;
    public IncidentRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<IncidentResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listIncident(q,status,filter,pageable); }
    @GetMapping("/{id}") public IncidentResponse get(@PathVariable Long id) { return service.getIncident(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentRequest r) { var result=service.createIncident(r); return ResponseEntity.created(URI.create("/api/incidents/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public IncidentResponse update(@PathVariable Long id, @Valid @RequestBody IncidentRequest r) { return service.updateIncident(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteIncident(id); }
}
