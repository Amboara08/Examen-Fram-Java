package com.example.tournois.service.impl;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.tournois.service.ApplicationService;
import com.example.tournois.entity.*;
import com.example.tournois.repository.*;
import com.example.tournois.mapper.*;
import com.example.tournois.dto.request.*;
import com.example.tournois.dto.response.*;
import com.example.tournois.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.ui.Model;
import jakarta.persistence.criteria.*;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
@Service @Transactional @Validated
public class ApplicationServiceImpl implements ApplicationService {
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final RegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    private final SystemStateRepository state;
    private final AuditEventRepository auditRepository;
    private final SettingRepository settingRepository;
    public ApplicationServiceImpl(TeamRepository teamRepository, TournamentRepository tournamentRepository, RegistrationRepository registrationRepository, MatchRepository matchRepository, PlayerRepository playerRepository,SystemStateRepository state,AuditEventRepository auditRepository,SettingRepository settingRepository) {
        this.teamRepository=teamRepository;
        this.tournamentRepository=tournamentRepository;
        this.registrationRepository=registrationRepository;
        this.matchRepository=matchRepository;
        this.playerRepository=playerRepository;

        this.state=state; this.auditRepository=auditRepository; this.settingRepository=settingRepository;
    }
    private String settingLabel(String name){return switch(name){
        case "points.win" -> "Points par victoire";
        case "points.draw" -> "Points par match nul";
        case "points.loss" -> "Points par défaite";
        default -> name.startsWith("rate.zone.") ? "Zone "+name.substring(10)+" : coefficient" : name.startsWith("price.zone.") ? "Zone "+name.substring(11)+" : coefficient" : name.startsWith("rate.vehicle.") ? "Véhicule "+name.substring(13)+" : coefficient" : name;
    };}
    private void lock(){state.lock().orElseThrow(()->new InvalidOperationException("Initialisation manquante"));}
    private <E> E require(JpaRepository<E,Long> repository,Long id){if(id==null) throw new InvalidOperationException("Identifiant obligatoire");return repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Enregistrement introuvable : "+id));}
    private void check(boolean valid,String message){if(!valid)throw new InvalidOperationException(message);}
    private void conflict(boolean valid,String message){if(!valid)throw new ConflictException(message);}
    private void audit(String resource,Long id,String action){var auth=SecurityContextHolder.getContext().getAuthentication();auditRepository.save(new AuditEvent(resource,id,action,auth==null ? "system" : auth.getName()));}
    @Override @Transactional(readOnly=true) public List<AuditResponse> history(String resource,Long id){return auditRepository.findByResourceAndResourceIdOrderByOccurredAtDesc(resource,id).stream().map(e->new AuditResponse(e.getAction(),e.getActor(),e.getOccurredAt())).toList();}
    @Override @Transactional(readOnly=true) public List<SettingResponse> settings(){return settingRepository.findAll().stream().sorted(Comparator.comparing(Setting::getName)).map(s->new SettingResponse(s.getName(),settingLabel(s.getName()),s.getValue())).toList();}
    @Override public void setting(SettingRequest r){lock();check(DEFAULTS.containsKey(r.name()),"Paramètre inconnu");check(!Set.of("commission","rate.subscriber").contains(r.name()) || r.value()<=1,"Ce taux doit être compris entre 0 et 1");check(!r.name().startsWith("points.") || r.value()==Math.floor(r.value()),"Les points doivent être entiers");settingRepository.save(new Setting(r.name(),r.value()));audit("Setting",0L,r.name()+" = "+r.value());}
    private double cfg(String name){return settingRepository.findById(name).map(Setting::getValue).orElse(DEFAULTS.getOrDefault(name,0.0));}
    private BigDecimal money(double value){return BigDecimal.valueOf(value).setScale(2,RoundingMode.HALF_UP);}
    private boolean between(LocalDate date,LocalDate from,LocalDate to){return (from==null || !date.isBefore(from)) && (to==null || !date.isAfter(to));}
    private void dates(LocalDate from,LocalDate to){check(from==null || to==null || !to.isBefore(from),"Période invalide");}
    private Pageable safePage(Pageable pageable,Set<String> allowed){
        var sort=pageable.getSort().isUnsorted()?Sort.by(Sort.Direction.DESC,"id"):pageable.getSort();
        for(var order:sort)check(allowed.contains(order.getProperty()),"Tri inconnu : "+order.getProperty());
        return PageRequest.of(Math.max(0,pageable.getPageNumber()),Math.min(100,Math.max(1,pageable.getPageSize())),sort);
    }
    private <E> Specification<E> spec(String q,String status,String filter,List<String> texts,Set<String> allowed){
        return (root,query,cb)->{
            var all=new ArrayList<Predicate>();
            if(q!=null && !q.isBlank()) {String value="%"+q.toLowerCase(Locale.ROOT).replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";all.add(cb.or(texts.stream().map(name->cb.like(cb.lower(root.get(name)),value,'\\')).toArray(Predicate[]::new)));}
            if(status!=null && !status.isBlank()){check(allowed.contains("status"),"Cette liste ne possède pas de statut");all.add(cb.equal(root.get("status"),status));}
            if(filter!=null && !filter.isBlank()) for(String part:filter.split(";")) {var pair=part.split(":",2);check(pair.length==2 && allowed.contains(pair[0]),"Filtre invalide");var path=root.get(pair[0]);Class<?> type=path.getJavaType();Object value=pair[1];try{if(type==Long.class)value=Long.valueOf(pair[1]);else if(type==Integer.class)value=Integer.valueOf(pair[1]);else if(type==BigDecimal.class)value=new BigDecimal(pair[1]);else if(type==LocalDate.class)value=LocalDate.parse(pair[1]);else if(type==LocalDateTime.class)value=LocalDateTime.parse(pair[1]);}catch(Exception e){throw new InvalidOperationException("Valeur de filtre invalide");}all.add(cb.equal(path,value));}
            return cb.and(all.toArray(Predicate[]::new));
        };
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<TeamResponse> listTeam(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","name","logo","city","coach","players","status");
        return PageResponse.of(teamRepository.findAll(spec(q,status,filter,List.of("name","logo","city","coach","players","status"),allowed),safePage(pageable,allowed)).map(TeamMapper::response));
    }
    @Override @Transactional(readOnly=true) public TeamResponse getTeam(Long id) {return TeamMapper.response(require(teamRepository,id));}
    @Override @Transactional(readOnly=true) public List<PlayerResponse> listPlayers(Long teamId) {
        require(teamRepository,teamId);
        return playerRepository.findByTeamIdOrderByNumberAsc(teamId).stream()
            .map(p->new PlayerResponse(p.getId(),p.getTeamId(),p.getNumber(),p.getName(),p.getHeightCm(),p.getPosition())).toList();
    }
    @Override public PlayerResponse addPlayer(Long teamId,PlayerRequest request) {
        lock(); require(teamRepository,teamId);
        conflict(!playerRepository.existsByTeamIdAndNumber(teamId,request.number()),"Ce numéro est déjà utilisé dans cette équipe");
        var p=new Player(); p.setTeamId(teamId); p.setNumber(request.number()); p.setName(request.name().trim());
        p.setHeightCm(request.heightCm()); p.setPosition(request.position()); playerRepository.saveAndFlush(p);
        audit("Team",teamId,"Ajout du joueur "+p.getName()+" (#"+p.getNumber()+")");
        return new PlayerResponse(p.getId(),p.getTeamId(),p.getNumber(),p.getName(),p.getHeightCm(),p.getPosition());
    }
    @Override public void deletePlayer(Long teamId,Long playerId) {
        lock(); require(teamRepository,teamId); var p=require(playerRepository,playerId);
        check(Objects.equals(p.getTeamId(),teamId),"Ce joueur n'appartient pas à cette équipe");
        playerRepository.delete(p); audit("Team",teamId,"Suppression du joueur "+p.getName());
    }
    private void validateTeam(Team e) {
        if(e.getStatus()==null || e.getStatus().isBlank()) e.setStatus("ACTIVE");
        if(teamRepository.findAll().stream().anyMatch(v->!Objects.equals(v.getId(),e.getId()) && Objects.equals(v.getName(),e.getName()))) throw new ConflictException("Nom déjà utilisé");
        validate(e);
    }
    @Override public TeamResponse createTeam(TeamRequest request) {
        lock(); var e=new Team(); TeamMapper.apply(request,e); validateTeam(e);
        teamRepository.saveAndFlush(e); afterCreate(e); audit("Team",e.getId(),"Création"); return TeamMapper.response(e);
    }
    @Override public TeamResponse updateTeam(Long id,TeamRequest request) {
        lock(); var e=require(teamRepository,id); beforeUpdate(e); TeamMapper.apply(request,e); validateTeam(e);
        teamRepository.saveAndFlush(e); audit("Team",id,"Modification"); return TeamMapper.response(e);
    }
    @Override public void deleteTeam(Long id) {
        lock(); var e=require(teamRepository,id); beforeDelete(e);
        if(registrationRepository.findAll().stream().anyMatch(v->Objects.equals(v.getTeamId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Inscriptions");
        if(matchRepository.findAll().stream().anyMatch(v->Objects.equals(v.getHomeTeamId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Matchs");
        if(matchRepository.findAll().stream().anyMatch(v->Objects.equals(v.getAwayTeamId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Matchs");
        playerRepository.deleteByTeamId(id);
        teamRepository.delete(e); audit("Team",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<TournamentResponse> listTournament(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","name","sport","description","startDate","endDate","location","maxTeams","format","rankingCriteria","status");
        return PageResponse.of(tournamentRepository.findAll(spec(q,status,filter,List.of("name","sport","description","location","format","rankingCriteria","status"),allowed),safePage(pageable,allowed)).map(TournamentMapper::response));
    }
    @Override @Transactional(readOnly=true) public TournamentResponse getTournament(Long id) {return TournamentMapper.response(require(tournamentRepository,id));}
    private void validateTournament(Tournament e) {
        if(e.getFormat()==null || e.getFormat().isBlank()) e.setFormat("CHAMPIONNAT");
        if(e.getRankingCriteria()==null || e.getRankingCriteria().isBlank()) e.setRankingCriteria("POINTS,DIFFERENCE,BUTS,CONFRONTATION");
        validate(e);
    }
    @Override public TournamentResponse createTournament(TournamentRequest request) {
        lock(); var e=new Tournament(); TournamentMapper.apply(request,e); validateTournament(e);
        tournamentRepository.saveAndFlush(e); afterCreate(e); audit("Tournament",e.getId(),"Création"); return TournamentMapper.response(e);
    }
    @Override public TournamentResponse updateTournament(Long id,TournamentRequest request) {
        lock(); var e=require(tournamentRepository,id); beforeUpdate(e); TournamentMapper.apply(request,e); validateTournament(e);
        tournamentRepository.saveAndFlush(e); audit("Tournament",id,"Modification"); return TournamentMapper.response(e);
    }
    @Override public void deleteTournament(Long id) {
        lock(); var e=require(tournamentRepository,id); beforeDelete(e);
        if(registrationRepository.findAll().stream().anyMatch(v->Objects.equals(v.getTournamentId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Inscriptions");
        if(matchRepository.findAll().stream().anyMatch(v->Objects.equals(v.getTournamentId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Matchs");
        tournamentRepository.delete(e); audit("Tournament",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<RegistrationResponse> listRegistration(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","tournamentId","teamId");
        return PageResponse.of(registrationRepository.findAll(spec(q,status,filter,List.of(),allowed),safePage(pageable,allowed)).map(RegistrationMapper::response));
    }
    @Override @Transactional(readOnly=true) public RegistrationResponse getRegistration(Long id) {return RegistrationMapper.response(require(registrationRepository,id));}
    private void validateRegistration(Registration e) {
        if(e.getTournamentId()!=null) require(tournamentRepository,e.getTournamentId());
        if(e.getTeamId()!=null) require(teamRepository,e.getTeamId());
        validate(e);
    }
    @Override public RegistrationResponse createRegistration(RegistrationRequest request) {
        lock(); var e=new Registration(); RegistrationMapper.apply(request,e); validateRegistration(e);
        registrationRepository.saveAndFlush(e); afterCreate(e); audit("Registration",e.getId(),"Création"); return RegistrationMapper.response(e);
    }
    @Override public RegistrationResponse updateRegistration(Long id,RegistrationRequest request) {
        lock(); var e=require(registrationRepository,id); beforeUpdate(e); RegistrationMapper.apply(request,e); validateRegistration(e);
        registrationRepository.saveAndFlush(e); audit("Registration",id,"Modification"); return RegistrationMapper.response(e);
    }
    @Override public void deleteRegistration(Long id) {
        lock(); var e=require(registrationRepository,id); beforeDelete(e);
        registrationRepository.delete(e); audit("Registration",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<MatchResponse> listMatch(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","tournamentId","homeTeamId","awayTeamId","scheduledAt","location","roundNumber","position","status","homeScore","awayScore","winnerId");
        return PageResponse.of(matchRepository.findAll(spec(q,status,filter,List.of("location","status"),allowed),safePage(pageable,allowed)).map(MatchMapper::response));
    }
    @Override @Transactional(readOnly=true) public MatchResponse getMatch(Long id) {return MatchMapper.response(require(matchRepository,id));}
    private void validateMatch(Match e) {
        if(e.getTournamentId()!=null) require(tournamentRepository,e.getTournamentId());
        if(e.getHomeTeamId()!=null) require(teamRepository,e.getHomeTeamId());
        if(e.getAwayTeamId()!=null) require(teamRepository,e.getAwayTeamId());
        validate(e);
    }
    @Override public MatchResponse createMatch(MatchRequest request) {
        lock(); var e=new Match(); MatchMapper.apply(request,e); validateMatch(e);
        matchRepository.saveAndFlush(e); afterCreate(e); audit("Match",e.getId(),"Création"); return MatchMapper.response(e);
    }
    @Override public MatchResponse updateMatch(Long id,MatchRequest request) {
        lock(); var e=require(matchRepository,id); beforeUpdate(e); MatchMapper.apply(request,e); validateMatch(e);
        matchRepository.saveAndFlush(e); audit("Match",id,"Modification"); return MatchMapper.response(e);
    }
    @Override public void deleteMatch(Long id) {
        lock(); var e=require(matchRepository,id); beforeDelete(e);
        matchRepository.delete(e); audit("Match",id,"Suppression");
    }

    public static final Map<String,Double> DEFAULTS=Map.of("points.win",3.0,"points.draw",1.0,"points.loss",0.0);
    private void editable(Tournament t){check(!Set.of("TERMINE","ANNULE").contains(t.getStatus()),"Ce tournoi est clôturé");}
    private void registrationsOpen(Tournament t){check(Set.of("BROUILLON","INSCRIPTION").contains(t.getStatus()),"Les inscriptions sont fermées");}
    private void validate(Object obj){
        if(obj instanceof Tournament t){
            editable(t); check(!t.getEndDate().isBefore(t.getStartDate()),"La fin précède le début");check(t.getMaxTeams()>=2,"Deux équipes minimum");
            var criteria=t.getRankingCriteria().split(",");check(Arrays.stream(criteria).allMatch(x->Set.of("POINTS","DIFFERENCE","BUTS","CONFRONTATION").contains(x)),"Critère de classement inconnu");
            check(registrationRepository.findAll().stream().filter(r->Objects.equals(r.getTournamentId(),t.getId())).count()<=t.getMaxTeams(),"Capacité inférieure aux inscriptions");
        }
        if(obj instanceof Registration r){var t=require(tournamentRepository,r.getTournamentId());registrationsOpen(t);check("ACTIVE".equals(require(teamRepository,r.getTeamId()).getStatus()),"Équipe inactive");
            conflict(registrationRepository.findAll().stream().noneMatch(v->!Objects.equals(v.getId(),r.getId()) && Objects.equals(v.getTournamentId(),r.getTournamentId()) && Objects.equals(v.getTeamId(),r.getTeamId())),"Équipe déjà inscrite");
            check(registrationRepository.findAll().stream().filter(v->Objects.equals(v.getTournamentId(),r.getTournamentId()) && !Objects.equals(v.getId(),r.getId())).count()<t.getMaxTeams(),"Tournoi complet");}
        if(obj instanceof Match m){var t=require(tournamentRepository,m.getTournamentId());editable(t);check(Set.of("PROGRAMME","EN_COURS").contains(t.getStatus()),"Générez le calendrier avant de planifier un match");
            check(m.getHomeTeamId()!=null && m.getAwayTeamId()!=null && !m.getHomeTeamId().equals(m.getAwayTeamId()),"Deux équipes distinctes sont nécessaires");
            var registered=registrationRepository.findAll().stream().filter(r->r.getTournamentId().equals(t.getId())).map(Registration::getTeamId).toList();check(registered.containsAll(List.of(m.getHomeTeamId(),m.getAwayTeamId())),"Équipe non inscrite");
            check(!m.getScheduledAt().toLocalDate().isBefore(t.getStartDate()) && !m.getScheduledAt().toLocalDate().isAfter(t.getEndDate()),"Match hors période du tournoi");
            for(var v:matchRepository.findAll()) if(!Objects.equals(v.getId(),m.getId())){
                conflict(!(v.getTournamentId().equals(m.getTournamentId()) && v.getRoundNumber().equals(m.getRoundNumber()) && v.getHomeTeamId()!=null && v.getAwayTeamId()!=null && Set.of(v.getHomeTeamId(),v.getAwayTeamId()).equals(Set.of(m.getHomeTeamId(),m.getAwayTeamId()))),"Match déjà présent");
                var teams=List.of(m.getHomeTeamId(),m.getAwayTeamId());boolean shared=teams.contains(v.getHomeTeamId()) || teams.contains(v.getAwayTeamId());conflict(!shared || Math.abs(Duration.between(v.getScheduledAt(),m.getScheduledAt()).toMinutes())>=120,"Une équipe joue déjà à cet horaire");}
        }
    }
    private void beforeUpdate(Object e){
        if(e instanceof Tournament t){registrationsOpen(t);}
        if(e instanceof Registration)throw new InvalidOperationException("Retirez puis réinscrivez l'équipe");
        if(e instanceof Match m){check(!"ELIMINATION".equals(require(tournamentRepository,m.getTournamentId()).getFormat()),"Le tableau d'élimination est calculé automatiquement");check(!"TERMINE".equals(m.getStatus()),"Utilisez la saisie de résultat");}
        if(e instanceof Team t) for(var r:registrationRepository.findAll()) if(r.getTeamId().equals(t.getId()))registrationsOpen(require(tournamentRepository,r.getTournamentId()));
    }
    private void beforeDelete(Object e){
        if(e instanceof Tournament t)registrationsOpen(t);
        if(e instanceof Registration r){registrationsOpen(require(tournamentRepository,r.getTournamentId()));audit("Team",r.getTeamId(),"Retrait du tournoi "+r.getTournamentId());}
        if(e instanceof Match m){editable(require(tournamentRepository,m.getTournamentId()));throw new InvalidOperationException("Un calendrier généré doit rester complet");}
    }
    private void afterCreate(Object e){if(e instanceof Registration r)audit("Team",r.getTeamId(),"Inscription au tournoi "+r.getTournamentId());}
    private List<Match> matches(Long id){return matchRepository.findAll().stream().filter(m->m.getTournamentId().equals(id)).sorted(Comparator.comparing(Match::getRoundNumber).thenComparing(Match::getPosition)).toList();}
    private Match match(Tournament t,Long home,Long away,int round,int position){
        var m=new Match();m.setTournamentId(t.getId());m.setHomeTeamId(home);m.setAwayTeamId(away);m.setRoundNumber(round);m.setPosition(position);m.setLocation(t.getLocation());m.setScheduledAt(t.getStartDate().atTime(9,0).plusHours((round-1)*2L));
        check(!m.getScheduledAt().toLocalDate().isAfter(t.getEndDate()),"Période trop courte pour le calendrier");checkSchedule(m);return matchRepository.save(m);
    }
    private void generate(Tournament t){
        registrationsOpen(t);conflict(matches(t.getId()).isEmpty(),"Calendrier déjà généré");
        var ids=new ArrayList<>(registrationRepository.findAll().stream().filter(r->r.getTournamentId().equals(t.getId())).sorted(Comparator.comparing(Registration::getId)).map(Registration::getTeamId).toList());check(ids.size()>=2,"Inscrivez au moins deux équipes");
        if("ELIMINATION".equals(t.getFormat())){
            int capacity=2;while(capacity<ids.size())capacity*=2;int byes=capacity-ids.size(),cursor=0;
            for(int pos=0;pos<capacity/2;pos++){Long home=ids.get(cursor++),away=pos<byes?null:ids.get(cursor++);var m=match(t,home,away,1,pos);if(away==null){m.setStatus("EXEMPT");m.setWinnerId(home);}}
            int count=capacity/4,round=2;while(count>0){for(int pos=0;pos<count;pos++)match(t,null,null,round,pos);round++;count/=2;}propagate(t);
        }else{
            if(ids.size()%2==1)ids.add(null);int n=ids.size(),rounds=n-1;
            for(int round=0;round<rounds;round++){
                for(int pos=0;pos<n/2;pos++){Long a=ids.get(pos),b=ids.get(n-1-pos);if(a!=null && b!=null){if(round%2==1){Long tmp=a;a=b;b=tmp;}match(t,a,b,round+1,pos);if("ALLER_RETOUR".equals(t.getFormat()))match(t,b,a,round+rounds+1,pos);}}
                ids.add(1,ids.remove(n-1));
            }
        }t.setStatus("PROGRAMME");audit("Tournament",t.getId(),"Calendrier généré");
    }
    private void propagate(Tournament t){
        var list=matches(t.getId());for(var m:list) if(m.getRoundNumber()>1){var predecessors=list.stream().filter(v->v.getRoundNumber()==m.getRoundNumber()-1 && v.getPosition()/2==m.getPosition()).sorted(Comparator.comparing(Match::getPosition)).toList();
            if(predecessors.size()==2){m.setHomeTeamId(predecessors.get(0).getWinnerId());m.setAwayTeamId(predecessors.get(1).getWinnerId());checkSchedule(m);}}
    }
    private void checkSchedule(Match match){
        if(match.getHomeTeamId()==null || match.getAwayTeamId()==null)return;
        for(var other:matchRepository.findAll())if(!Objects.equals(other.getId(),match.getId()) && other.getHomeTeamId()!=null && other.getAwayTeamId()!=null && !"EXEMPT".equals(other.getStatus())){
            boolean shared=Objects.equals(match.getHomeTeamId(),other.getHomeTeamId()) || Objects.equals(match.getHomeTeamId(),other.getAwayTeamId()) || Objects.equals(match.getAwayTeamId(),other.getHomeTeamId()) || Objects.equals(match.getAwayTeamId(),other.getAwayTeamId());
            conflict(!shared || Math.abs(Duration.between(match.getScheduledAt(),other.getScheduledAt()).toMinutes())>=120,"Une équipe joue déjà dans un autre match à cet horaire");
        }
    }
    private void result(Match m,ActionRequest r){
        var t=require(tournamentRepository,m.getTournamentId());check("EN_COURS".equals(t.getStatus()),"Démarrez le tournoi avant de saisir les résultats");
        check(m.getHomeTeamId()!=null && m.getAwayTeamId()!=null,"Les équipes ne sont pas encore qualifiées");check(r.homeScore()!=null && r.awayScore()!=null,"Les deux scores sont obligatoires");
        if("ELIMINATION".equals(t.getFormat())){check(!r.homeScore().equals(r.awayScore()),"Un match éliminatoire doit avoir un vainqueur");check(matches(t.getId()).stream().noneMatch(v->v.getRoundNumber()>m.getRoundNumber() && v.getHomeScore()!=null),"Un tour suivant possède déjà un résultat");}
        m.setHomeScore(r.homeScore());m.setAwayScore(r.awayScore());m.setStatus("TERMINE");m.setWinnerId(r.homeScore().equals(r.awayScore())?null:r.homeScore()>r.awayScore()?m.getHomeTeamId():m.getAwayTeamId());
        if("ELIMINATION".equals(t.getFormat()))propagate(t);audit("Match",m.getId(),"Résultat : "+r.homeScore()+" - "+r.awayScore());audit("Tournament",t.getId(),"Résultat du match "+m.getId());
    }
    @Override public Object perform(String resource,Long id,String operation,ActionRequest r){
        lock();if("Match".equals(resource) && "result".equals(operation)){var m=require(matchRepository,id);result(m,r);return MatchMapper.response(m);}
        var t=require(tournamentRepository,id);editable(t);
        switch(operation){
            case "generate-matches"->generate(t);
            case "teams"->{check(r.teamId()!=null,"Choisissez une équipe");return createRegistration(new RegistrationRequest(id,r.teamId()));}
            case "remove-team"->{check(r.teamId()!=null,"Choisissez une équipe");var registration=registrationRepository.findAll().stream().filter(v->v.getTournamentId().equals(id) && v.getTeamId().equals(r.teamId())).findFirst().orElseThrow(()->new ResourceNotFoundException("Inscription introuvable"));deleteRegistration(registration.getId());}
            case "status"->{check(r.status()!=null,"Choisissez un statut");var transitions=Map.of("BROUILLON",List.of("INSCRIPTION","ANNULE"),"INSCRIPTION",List.of("ANNULE"),"PROGRAMME",List.of("EN_COURS","ANNULE"),"EN_COURS",List.of("TERMINE","ANNULE"));check(transitions.getOrDefault(t.getStatus(),List.of()).contains(r.status()),"Transition de tournoi interdite");if("TERMINE".equals(r.status()))check(matches(id).stream().allMatch(m->Set.of("TERMINE","EXEMPT").contains(m.getStatus())),"Des matchs restent à jouer");t.setStatus(r.status());audit(resource,id,"Statut : "+r.status());}
            default->throw new InvalidOperationException("Action inconnue");
        }return TournamentMapper.response(t);
    }
    @Override @Transactional(readOnly=true) public List<RankingResponse> ranking(Long id){
        var t=require(tournamentRepository,id);var rows=new LinkedHashMap<Long,int[]>();registrationRepository.findAll().stream().filter(r->r.getTournamentId().equals(id)).forEach(r->rows.put(r.getTeamId(),new int[8]));
        for(var m:matches(id))if(m.getHomeScore()!=null && m.getAwayScore()!=null){int a=m.getHomeScore(),b=m.getAwayScore();var h=rows.get(m.getHomeTeamId());var v=rows.get(m.getAwayTeamId());h[0]++;v[0]++;h[5]+=a;h[6]+=b;v[5]+=b;v[6]+=a;if(a>b){h[1]++;v[3]++;h[4]+=(int)cfg("points.win");v[4]+=(int)cfg("points.loss");}else if(a<b){v[1]++;h[3]++;v[4]+=(int)cfg("points.win");h[4]+=(int)cfg("points.loss");}else{h[2]++;v[2]++;h[4]+=(int)cfg("points.draw");v[4]+=(int)cfg("points.draw");}}
        for(var e:rows.entrySet())for(var m:matches(id))if(m.getHomeScore()!=null && rows.get(m.getHomeTeamId())[4]==e.getValue()[4] && rows.get(m.getAwayTeamId())[4]==e.getValue()[4]){boolean home=e.getKey().equals(m.getHomeTeamId()),away=e.getKey().equals(m.getAwayTeamId());if(home||away){int own=home?m.getHomeScore():m.getAwayScore(),other=home?m.getAwayScore():m.getHomeScore();e.getValue()[7]+=own>other?(int)cfg("points.win"):own==other?(int)cfg("points.draw"):(int)cfg("points.loss");}}
        var ordered=new ArrayList<>(rows.keySet());ordered.sort((a,b)->{int[] h=rows.get(a),v=rows.get(b);for(String criterion:t.getRankingCriteria().split(",")){int diff=switch(criterion){case "POINTS"->Integer.compare(v[4],h[4]);case "DIFFERENCE"->Integer.compare(v[5]-v[6],h[5]-h[6]);case "BUTS"->Integer.compare(v[5],h[5]);case "CONFRONTATION"->Integer.compare(v[7],h[7]);default->0;};if(diff!=0)return diff;}return a.compareTo(b);});
        return ordered.stream().map(key->{var r=rows.get(key);return new RankingResponse(key,require(teamRepository,key).getName(),r[0],r[1],r[2],r[3],r[4],r[5],r[6],r[5]-r[6]);}).toList();
    }
    @Override @Transactional(readOnly=true) public AnalyticsResponse statistics(LocalDate from,LocalDate to,Long actorId,String zone){
        dates(from,to);var ts=tournamentRepository.findAll().stream().filter(t->between(t.getStartDate(),from,to)).toList();var ids=ts.stream().map(Tournament::getId).toList();var ms=matchRepository.findAll().stream().filter(m->ids.contains(m.getTournamentId())).toList();var metrics=new LinkedHashMap<String,Number>();metrics.put("Tournois",ts.size());metrics.put("Équipes",teamRepository.count());metrics.put("Matchs",ms.size());metrics.put("Matchs terminés",ms.stream().filter(m->"TERMINE".equals(m.getStatus())).count());metrics.put("Buts",ms.stream().filter(m->m.getHomeScore()!=null).mapToInt(m->m.getHomeScore()+m.getAwayScore()).sum());return new AnalyticsResponse(metrics,Map.of());
    }
    @Override @Transactional(readOnly=true) public void detail(String resource,Long id,Model m){if("Match".equals(resource))m.addAttribute("tournament",getTournament(require(matchRepository,id).getTournamentId()));if("Tournament".equals(resource)){m.addAttribute("matches",matches(id).stream().map(MatchMapper::response).toList());m.addAttribute("ranking",ranking(id));m.addAttribute("registrations",registrationRepository.findAll().stream().filter(r->r.getTournamentId().equals(id)).map(RegistrationMapper::response).toList());m.addAttribute("teams",listTeam("","","",PageRequest.of(0,100)).content());}}

}
