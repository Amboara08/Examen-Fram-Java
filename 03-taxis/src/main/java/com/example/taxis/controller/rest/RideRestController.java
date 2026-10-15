package com.example.taxis.controller.rest;

import com.example.taxis.service.ApplicationService;
import com.example.taxis.dto.request.*;
import com.example.taxis.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;
@RestController
@RequestMapping("/api/rides")
public class RideRestController {
    private final ApplicationService service;
    public RideRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<RideResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listRide(q,status,filter,pageable); }
    @GetMapping("/{id}") public RideResponse get(@PathVariable Long id) { return service.getRide(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<RideResponse> create(@Valid @RequestBody RideRequest r) { var result=service.createRide(r); return ResponseEntity.created(URI.create("/api/rides/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public RideResponse update(@PathVariable Long id, @Valid @RequestBody RideRequest r) { return service.updateRide(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteRide(id); }
}
