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
@RequestMapping("/api/matches")
public class MatchRestController {
    private final ApplicationService service;
    public MatchRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<MatchResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listMatch(q,status,filter,pageable); }
    @GetMapping("/{id}") public MatchResponse get(@PathVariable Long id) { return service.getMatch(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody MatchRequest r) { var result=service.createMatch(r); return ResponseEntity.created(URI.create("/api/matches/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public MatchResponse update(@PathVariable Long id, @Valid @RequestBody MatchRequest r) { return service.updateMatch(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteMatch(id); }
}
