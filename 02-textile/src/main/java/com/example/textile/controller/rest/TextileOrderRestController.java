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
@RequestMapping("/api/orders")
public class TextileOrderRestController {
    private final ApplicationService service;
    public TextileOrderRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<TextileOrderResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listTextileOrder(q,status,filter,pageable); }
    @GetMapping("/{id}") public TextileOrderResponse get(@PathVariable Long id) { return service.getTextileOrder(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<TextileOrderResponse> create(@Valid @RequestBody TextileOrderRequest r) { var result=service.createTextileOrder(r); return ResponseEntity.created(URI.create("/api/orders/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TextileOrderResponse update(@PathVariable Long id, @Valid @RequestBody TextileOrderRequest r) { return service.updateTextileOrder(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteTextileOrder(id); }
}
