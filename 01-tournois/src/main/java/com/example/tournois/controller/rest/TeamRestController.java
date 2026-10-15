package com.example.tournois.controller.rest;

import com.example.tournois.service.ApplicationService;
import com.example.tournois.dto.request.*;
import com.example.tournois.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;
@RestController
@RequestMapping("/api/teams")
public class TeamRestController {
    private final ApplicationService service;
    public TeamRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<TeamResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listTeam(q,status,filter,pageable); }
    @GetMapping("/{id}") public TeamResponse get(@PathVariable Long id) { return service.getTeam(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest r) { var result=service.createTeam(r); return ResponseEntity.created(URI.create("/api/teams/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamRequest r) { return service.updateTeam(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteTeam(id); }
}
