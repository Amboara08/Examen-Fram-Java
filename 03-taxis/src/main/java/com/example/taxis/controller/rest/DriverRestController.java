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
@RequestMapping("/api/drivers")
public class DriverRestController {
    private final ApplicationService service;
    public DriverRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<DriverResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listDriver(q,status,filter,pageable); }
    @GetMapping("/{id}") public DriverResponse get(@PathVariable Long id) { return service.getDriver(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody DriverRequest r) { var result=service.createDriver(r); return ResponseEntity.created(URI.create("/api/drivers/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public DriverResponse update(@PathVariable Long id, @Valid @RequestBody DriverRequest r) { return service.updateDriver(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteDriver(id); }
}
