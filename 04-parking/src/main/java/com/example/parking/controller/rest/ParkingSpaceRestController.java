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
@RequestMapping("/api/parking/spaces")
public class ParkingSpaceRestController {
    private final ApplicationService service;
    public ParkingSpaceRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<ParkingSpaceResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listParkingSpace(q,status,filter,pageable); }
    @GetMapping("/{id}") public ParkingSpaceResponse get(@PathVariable Long id) { return service.getParkingSpace(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<ParkingSpaceResponse> create(@Valid @RequestBody ParkingSpaceRequest r) { var result=service.createParkingSpace(r); return ResponseEntity.created(URI.create("/api/parking/spaces/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ParkingSpaceResponse update(@PathVariable Long id, @Valid @RequestBody ParkingSpaceRequest r) { return service.updateParkingSpace(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteParkingSpace(id); }
}
