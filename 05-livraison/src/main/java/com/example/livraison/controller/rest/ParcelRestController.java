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
@RequestMapping("/api/parcels")
public class ParcelRestController {
    private final ApplicationService service;
    public ParcelRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<ParcelResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listParcel(q,status,filter,pageable); }
    @GetMapping("/{id}") public ParcelResponse get(@PathVariable Long id) { return service.getParcel(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<ParcelResponse> create(@Valid @RequestBody ParcelRequest r) { var result=service.createParcel(r); return ResponseEntity.created(URI.create("/api/parcels/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ParcelResponse update(@PathVariable Long id, @Valid @RequestBody ParcelRequest r) { return service.updateParcel(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteParcel(id); }
}
