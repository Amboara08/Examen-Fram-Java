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
@RequestMapping("/api/vehicles")
public class VehicleRestController {
    private final ApplicationService service;
    public VehicleRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<VehicleResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listVehicle(q,status,filter,pageable); }
    @GetMapping("/{id}") public VehicleResponse get(@PathVariable Long id) { return service.getVehicle(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest r) { var result=service.createVehicle(r); return ResponseEntity.created(URI.create("/api/vehicles/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody VehicleRequest r) { return service.updateVehicle(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteVehicle(id); }
}
