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
@RequestMapping("/api/tournaments")
public class TournamentRestController {
    private final ApplicationService service;
    public TournamentRestController(ApplicationService service) { this.service=service; }
    @GetMapping public PageResponse<TournamentResponse> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String status, @RequestParam(defaultValue="") String filter, Pageable pageable) { return service.listTournament(q,status,filter,pageable); }
    @GetMapping("/{id}") public TournamentResponse get(@PathVariable Long id) { return service.getTournament(id); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public ResponseEntity<TournamentResponse> create(@Valid @RequestBody TournamentRequest r) { var result=service.createTournament(r); return ResponseEntity.created(URI.create("/api/tournaments/"+result.id())).body(result); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TournamentResponse update(@PathVariable Long id, @Valid @RequestBody TournamentRequest r) { return service.updateTournament(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) { service.deleteTournament(id); }
}
