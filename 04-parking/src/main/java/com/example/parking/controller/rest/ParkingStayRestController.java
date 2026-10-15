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
@RequestMapping("/api/parking/stays")
public class ParkingStayRestController {
    private final ApplicationService service;
    public ParkingStayRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<ParkingStayResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listParkingStay(q,status,filter,pageable); }
    @GetMapping("/{id}") public ParkingStayResponse get(@PathVariable Long id) { return service.getParkingStay(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<ParkingStayResponse> create(@Valid @RequestBody ParkingStayRequest r) { var result=service.createParkingStay(r); return ResponseEntity.created(URI.create("/api/parking/stays/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ParkingStayResponse update(@PathVariable Long id, @Valid @RequestBody ParkingStayRequest r) { return service.updateParkingStay(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteParkingStay(id); }
}
