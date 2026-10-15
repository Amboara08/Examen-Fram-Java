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
@RequestMapping("/api/quality-controls")
public class QualityControlRestController {
    private final ApplicationService service;
    public QualityControlRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<QualityControlResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listQualityControl(q,status,filter,pageable); }
    @GetMapping("/{id}") public QualityControlResponse get(@PathVariable Long id) { return service.getQualityControl(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<QualityControlResponse> create(@Valid @RequestBody QualityControlRequest r) { var result=service.createQualityControl(r); return ResponseEntity.created(URI.create("/api/quality-controls/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public QualityControlResponse update(@PathVariable Long id, @Valid @RequestBody QualityControlRequest r) { return service.updateQualityControl(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteQualityControl(id); }
}
