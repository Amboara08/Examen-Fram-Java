package com.example.tournois;
import java.time.*;
import java.math.*;
import java.util.*;
import com.example.tournois.service.ApplicationService;
import com.example.tournois.dto.request.*;
import com.example.tournois.dto.response.*;
import com.example.tournois.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import org.springframework.data.domain.PageRequest;
@SpringBootTest @AutoConfigureMockMvc @WithMockUser(username="admin",roles="ADMIN")
class ApplicationTests {

    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    @org.junit.jupiter.api.BeforeEach void cleanDatabase(){jdbc.update("delete from match_records");jdbc.update("delete from registration_records");jdbc.update("delete from tournament_records");jdbc.update("delete from team_records");jdbc.update("delete from audit_event");}
    @Autowired ApplicationService service;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private String unique(){return UUID.randomUUID().toString().substring(0,8);}
    private BigDecimal bd(String value){return new BigDecimal(value);}
    private ActionRequest actionStatus(String status){return new ActionRequest(status,null,null,null,null,null,null,null,null,null,null,null);}
    private ActionRequest scores(int home,int away){return new ActionRequest(null,null,null,home,away,null,null,null,null,null,null,null);}
    private ActionRequest quantity(int quantity){return new ActionRequest(null,null,quantity,null,null,"Responsable","",null,null,null,null,null);}
    @Test void httpSecurityAndValidation() throws Exception {
        mvc.perform(get("/api/tournaments").with(anonymous())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/tournaments").with(user("user").roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tournaments/99999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/tournaments").param("size","2").param("sort","unknown,asc")).andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/settings").with(user("operator").roles("OPERATOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/tournaments").with(user("operator").roles("OPERATOR"))).andExpect(status().isForbidden());
    }
    @Test void pagesRender() throws Exception {
        for(String path:List.of("/","/statistics","/settings","/teams","/teams/new","/tournaments","/tournaments/new","/registrations","/registrations/new","/matches","/matches/new"))mvc.perform(get(path)).andExpect(status().isOk());
    }
    private TournamentResponse tournament(String format){return service.createTournament(new TournamentRequest("Coupe "+unique(),"Football","",LocalDate.now(),LocalDate.now().plusDays(30),"Stade",32,format,"POINTS,DIFFERENCE,BUTS,CONFRONTATION"));}
    private TeamResponse team(){return service.createTeam(new TeamRequest("Équipe "+unique(),"","Ville","Coach","A, B","ACTIVE"));}
    private void register(Long tournament,int count){for(int i=0;i<count;i++)service.createRegistration(new RegistrationRequest(tournament,team().id()));}
    private List<MatchResponse> matches(Long id){return service.listMatch("","","tournamentId:"+id,PageRequest.of(0,100)).content();}
    @Test void leagueAndRankingRecalculation() throws Exception {
        var t=tournament("CHAMPIONNAT");register(t.id(),4);var a=service.listRegistration("","","tournamentId:"+t.id(),PageRequest.of(0,10)).content().get(0);
        assertThrows(ConflictException.class,()->service.createRegistration(new RegistrationRequest(t.id(),a.teamId())));
        service.perform("Tournament",t.id(),"generate-matches",ActionRequest.empty());assertEquals(6,matches(t.id()).size());
        assertThrows(InvalidOperationException.class,()->service.createRegistration(new RegistrationRequest(t.id(),team().id())));
        service.perform("Tournament",t.id(),"status",actionStatus("EN_COURS"));var m=matches(t.id()).get(0);service.perform("Match",m.id(),"result",scores(2,1));assertEquals(3,service.ranking(t.id()).stream().filter(r->r.teamId().equals(m.homeTeamId())).findFirst().orElseThrow().points());
        service.perform("Match",m.id(),"result",scores(0,0));assertEquals(1,service.ranking(t.id()).stream().filter(r->r.teamId().equals(m.homeTeamId())).findFirst().orElseThrow().points());
        assertThrows(InvalidOperationException.class,()->service.perform("Tournament",t.id(),"status",actionStatus("TERMINE")));
        for(var match:matches(t.id()))service.perform("Match",match.id(),"result",scores(1,0));service.perform("Tournament",t.id(),"status",actionStatus("TERMINE"));
        assertThrows(InvalidOperationException.class,()->service.perform("Match",m.id(),"result",scores(3,0)));
        mvc.perform(get("/tournaments/"+t.id())).andExpect(status().isOk());
        mvc.perform(put("/api/tournaments/"+t.id()).with(user("user").roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(new TournamentRequest("Autre","Foot","",LocalDate.now(),LocalDate.now().plusDays(1),"Stade",8,"CHAMPIONNAT","POINTS")))).andExpect(status().isForbidden());
    }
    @Test void returnLegsOddTeamsAndKnockoutByes() throws Exception {
        var league=tournament("ALLER_RETOUR");register(league.id(),3);service.perform("Tournament",league.id(),"generate-matches",ActionRequest.empty());assertEquals(6,matches(league.id()).size());
        var t=tournament("ELIMINATION");register(t.id(),10);service.perform("Tournament",t.id(),"generate-matches",ActionRequest.empty());assertEquals(15,matches(t.id()).size());assertEquals(6,matches(t.id()).stream().filter(m->"EXEMPT".equals(m.status())).count());
        service.perform("Tournament",t.id(),"status",actionStatus("EN_COURS"));
        for(int round=1;round<=4;round++){int current=round;for(var m:matches(t.id()).stream().filter(v->v.roundNumber()==current&&!"EXEMPT".equals(v.status())).toList()){assertNotNull(m.homeTeamId());assertNotNull(m.awayTeamId());service.perform("Match",m.id(),"result",scores(1,0));}}
        service.perform("Tournament",t.id(),"status",actionStatus("TERMINE"));assertEquals("TERMINE",service.getTournament(t.id()).status());mvc.perform(get("/tournaments/"+t.id())).andExpect(status().isOk());
    }
    @Test void apiCreatesDtosAndMvcEdits() throws Exception {
        var req=new TeamRequest("HTTP "+unique(),"","Ville","Coach","","ACTIVE");var response=mvc.perform(post("/api/teams").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req))).andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn();Long id=json.readTree(response.getResponse().getContentAsString()).get("id").asLong();mvc.perform(get("/teams/"+id+"/edit")).andExpect(status().isOk());
        mvc.perform(post("/teams/"+id+"/edit").with(csrf()).param("name",req.name()).param("city","Autre ville").param("coach","Coach").param("status","ACTIVE")).andExpect(status().is3xxRedirection());assertEquals("Autre ville",service.getTeam(id).city());
    }

}
