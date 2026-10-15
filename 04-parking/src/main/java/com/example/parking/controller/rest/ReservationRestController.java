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
@RequestMapping("/api/reservations")
public class ReservationRestController {
    private final ApplicationService service;
    public ReservationRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<ReservationResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listReservation(q,status,filter,pageable); }
    @GetMapping("/{id}") public ReservationResponse get(@PathVariable Long id) { return service.getReservation(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest r) { var result=service.createReservation(r); return ResponseEntity.created(URI.create("/api/reservations/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ReservationResponse update(@PathVariable Long id, @Valid @RequestBody ReservationRequest r) { return service.updateReservation(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteReservation(id); }
}
