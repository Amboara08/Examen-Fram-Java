package com.example.tournois.controller.rest;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.tournois.service.ApplicationService;
import com.example.tournois.dto.request.*;
import com.example.tournois.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
@RestController @RequestMapping("/api") public class OperationRestController {
 private final ApplicationService service;
 public OperationRestController(ApplicationService service){this.service=service;}
 @PostMapping("/tournaments/{id}/generate-matches") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action0(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Tournament",id,"generate-matches",request==null?ActionRequest.empty():request);}
 @PostMapping("/tournaments/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action1(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Tournament",id,"status",request==null?ActionRequest.empty():request);}
 @PostMapping("/tournaments/{id}/teams") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action2(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Tournament",id,"teams",request==null?ActionRequest.empty():request);}
 @PutMapping("/matches/{id}/result") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") public Object action3(@PathVariable Long id,@Valid @RequestBody(required=false) ActionRequest request){return service.perform("Match",id,"result",request==null?ActionRequest.empty():request);}
 @GetMapping({"/statistics/revenue","/statistics/activity","/statistics/deliveries","/statistics"}) public AnalyticsResponse stats(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(required=false) Long actorId,@RequestParam(defaultValue="") String zone){return service.statistics(from,to,actorId,zone);}
 @GetMapping("/settings") @PreAuthorize("hasRole('ADMIN')") public List<SettingResponse> settings(){return service.settings();}
 @PutMapping("/settings") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void setting(@Valid @RequestBody SettingRequest r){service.setting(r);}
 @GetMapping("/tournaments/{id}/ranking") public List<RankingResponse> ranking(@PathVariable Long id){return service.ranking(id);}
 @GetMapping("/tournaments/{id}/matches") public PageResponse<MatchResponse> matches(@PathVariable Long id,org.springframework.data.domain.Pageable pageable){service.getTournament(id);return service.listMatch("","","tournamentId:"+id,pageable);}
 @DeleteMapping("/tournaments/{id}/teams/{teamId}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')") @ResponseStatus(HttpStatus.NO_CONTENT) public void removeTeam(@PathVariable Long id,@PathVariable Long teamId){service.perform("Tournament",id,"remove-team",new ActionRequest(null,teamId,null,null,null,null,null,null,null,null,null,null));}
}
