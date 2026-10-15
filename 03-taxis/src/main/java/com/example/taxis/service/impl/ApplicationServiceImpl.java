package com.example.taxis.service.impl;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.taxis.service.ApplicationService;
import com.example.taxis.entity.*;
import com.example.taxis.repository.*;
import com.example.taxis.mapper.*;
import com.example.taxis.dto.request.*;
import com.example.taxis.dto.response.*;
import com.example.taxis.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.ui.Model;
import jakarta.persistence.criteria.*;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
@Service @Transactional @Validated
public class ApplicationServiceImpl implements ApplicationService {
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;

    private final SystemStateRepository state;
    private final AuditEventRepository auditRepository;
    private final SettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;
    public ApplicationServiceImpl(ClientRepository clientRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository, RideRepository rideRepository,SystemStateRepository state,AuditEventRepository auditRepository,SettingRepository settingRepository,PasswordEncoder passwordEncoder) {
        this.clientRepository=clientRepository;
        this.vehicleRepository=vehicleRepository;
        this.driverRepository=driverRepository;
        this.rideRepository=rideRepository;

        this.state=state; this.auditRepository=auditRepository; this.settingRepository=settingRepository;this.passwordEncoder=passwordEncoder;
    }
    private String settingLabel(String name){return switch(name){
        case "fare.base" -> "Tarif de prise en charge";
        case "fare.km" -> "Tarif par kilomètre";
        case "commission" -> "Commission (0 à 1)";
        case "score.distance" -> "Score : poids de la distance";
        case "score.workload" -> "Score : poids des courses effectuées";
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
    public PageResponse<ClientResponse> listClient(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","name","email","phone","address");
        return PageResponse.of(clientRepository.findAll(spec(q,status,filter,List.of("name","email","phone","address"),allowed),safePage(pageable,allowed)).map(ClientMapper::response));
    }
    @Override @Transactional(readOnly=true) public ClientResponse getClient(Long id) {return ClientMapper.response(require(clientRepository,id));}
    @Override @Transactional(readOnly=true) public ClientResponse getClientByEmail(String email){return clientRepository.findByEmailIgnoreCase(email).map(ClientMapper::response).orElseThrow(()->new ResourceNotFoundException("Compte client introuvable"));}
    private void validateClient(Client e) {
        e.setEmail(e.getEmail().trim().toLowerCase(Locale.ROOT));
        if(clientRepository.findByEmailIgnoreCase(e.getEmail()).filter(v->!Objects.equals(v.getId(),e.getId())).isPresent())throw new ConflictException("Cette adresse e-mail est déjà utilisée");
        validate(e);
    }
    @Override public ClientResponse createClient(ClientRequest request) {
        lock(); var e=new Client(); ClientMapper.apply(request,e); validateClient(e);
        clientRepository.saveAndFlush(e); afterCreate(e); audit("Client",e.getId(),"Création"); return ClientMapper.response(e);
    }
    @Override public ClientResponse updateClient(Long id,ClientRequest request) {
        lock(); var e=require(clientRepository,id); beforeUpdate(e); ClientMapper.apply(request,e); validateClient(e);
        clientRepository.saveAndFlush(e); audit("Client",id,"Modification"); return ClientMapper.response(e);
    }
    @Override public void deleteClient(Long id) {
        lock(); var e=require(clientRepository,id); beforeDelete(e);
        if(rideRepository.findAll().stream().anyMatch(v->Objects.equals(v.getClientId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Courses");
        clientRepository.delete(e); audit("Client",id,"Suppression");
    }
    @Override public void setClientPassword(Long id,String password){lock();check(password!=null&&password.length()>=8,"Le mot de passe doit contenir au moins 8 caractères");var c=require(clientRepository,id);c.setPasswordHash(passwordEncoder.encode(password));clientRepository.saveAndFlush(c);audit("Client",id,"Mot de passe défini");}

    @Override @Transactional(readOnly=true)
    public PageResponse<VehicleResponse> listVehicle(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","plate","brand","model","year","capacity","mileage","status");
        return PageResponse.of(vehicleRepository.findAll(spec(q,status,filter,List.of("plate","brand","model","status"),allowed),safePage(pageable,allowed)).map(VehicleMapper::response));
    }
    @Override @Transactional(readOnly=true) public VehicleResponse getVehicle(Long id) {return VehicleMapper.response(require(vehicleRepository,id));}
    private void validateVehicle(Vehicle e) {
        if(e.getStatus()==null || e.getStatus().isBlank()) e.setStatus("DISPONIBLE");
        if(vehicleRepository.findAll().stream().anyMatch(v->!Objects.equals(v.getId(),e.getId()) && Objects.equals(v.getPlate(),e.getPlate()))) throw new ConflictException("Immatriculation déjà utilisé");
        validate(e);
    }
    @Override public VehicleResponse createVehicle(VehicleRequest request) {
        lock(); var e=new Vehicle(); VehicleMapper.apply(request,e); validateVehicle(e);
        vehicleRepository.saveAndFlush(e); afterCreate(e); audit("Vehicle",e.getId(),"Création"); return VehicleMapper.response(e);
    }
    @Override public VehicleResponse updateVehicle(Long id,VehicleRequest request) {
        lock(); var e=require(vehicleRepository,id); beforeUpdate(e); VehicleMapper.apply(request,e); validateVehicle(e);
        vehicleRepository.saveAndFlush(e); audit("Vehicle",id,"Modification"); return VehicleMapper.response(e);
    }
    @Override public void deleteVehicle(Long id) {
        lock(); var e=require(vehicleRepository,id); beforeDelete(e);
        if(driverRepository.findAll().stream().anyMatch(v->Objects.equals(v.getVehicleId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Chauffeurs");
        vehicleRepository.delete(e); audit("Vehicle",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<DriverResponse> listDriver(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","name","phone","licenseNumber","licenseExpiry","hiredAt","vehicleId","distanceToPickup","status");
        return PageResponse.of(driverRepository.findAll(spec(q,status,filter,List.of("name","phone","licenseNumber","status"),allowed),safePage(pageable,allowed)).map(DriverMapper::response));
    }
    @Override @Transactional(readOnly=true) public DriverResponse getDriver(Long id) {return DriverMapper.response(require(driverRepository,id));}
    @Override @Transactional(readOnly=true) public DriverResponse getDriverByEmail(String email){return driverRepository.findByEmailIgnoreCase(email).map(DriverMapper::response).orElseThrow(()->new ResourceNotFoundException("Compte chauffeur introuvable"));}
    private void validateDriver(Driver e) {
        if(e.getVehicleId()!=null) require(vehicleRepository,e.getVehicleId());
        if(e.getStatus()==null || e.getStatus().isBlank()) e.setStatus("DISPONIBLE");
        e.setEmail(e.getEmail().trim().toLowerCase(Locale.ROOT));
        if(driverRepository.findByEmailIgnoreCase(e.getEmail()).filter(v->!Objects.equals(v.getId(),e.getId())).isPresent())throw new ConflictException("Cette adresse e-mail est déjà utilisée");
        if(driverRepository.findAll().stream().anyMatch(v->!Objects.equals(v.getId(),e.getId()) && Objects.equals(v.getLicenseNumber(),e.getLicenseNumber()))) throw new ConflictException("Permis déjà utilisé");
        validate(e);
    }
    @Override public DriverResponse createDriver(DriverRequest request) {
        lock(); var e=new Driver(); DriverMapper.apply(request,e); validateDriver(e);
        driverRepository.saveAndFlush(e); afterCreate(e); audit("Driver",e.getId(),"Création"); return DriverMapper.response(e);
    }
    @Override public DriverResponse updateDriver(Long id,DriverRequest request) {
        lock(); var e=require(driverRepository,id); beforeUpdate(e); DriverMapper.apply(request,e); validateDriver(e);
        driverRepository.saveAndFlush(e); audit("Driver",id,"Modification"); return DriverMapper.response(e);
    }
    @Override public void deleteDriver(Long id) {
        lock(); var e=require(driverRepository,id); beforeDelete(e);
        var vehicleId=e.getVehicleId(); driverRepository.delete(e); driverRepository.flush();
        if(vehicleId!=null) vehicleRepository.delete(require(vehicleRepository,vehicleId));
        audit("Driver",id,"Suppression du chauffeur et de son véhicule");
    }
    @Override public void setDriverPassword(Long id,String password){lock();check(password!=null&&password.length()>=8,"Le mot de passe doit contenir au moins 8 caractères");var d=require(driverRepository,id);d.setPasswordHash(passwordEncoder.encode(password));driverRepository.saveAndFlush(d);audit("Driver",id,"Mot de passe défini");}
    @Override public DriverResponse createDriverWithVehicle(DriverRequest request,String password,VehicleRequest vehicleRequest){
        lock();check(password!=null&&password.length()>=8,"Le mot de passe doit contenir au moins 8 caractères");var v=new Vehicle();VehicleMapper.apply(vehicleRequest,v);validateVehicle(v);vehicleRepository.saveAndFlush(v);var d=new Driver();DriverMapper.apply(request,d);d.setVehicleId(v.getId());d.setPasswordHash(passwordEncoder.encode(password));validateDriver(d);driverRepository.saveAndFlush(d);audit("Driver",d.getId(),"Création avec véhicule");return DriverMapper.response(d);
    }
    @Override public DriverResponse updateDriverWithVehicle(Long id,DriverRequest request,String password,VehicleRequest vehicleRequest){
        lock();var d=require(driverRepository,id);beforeUpdate(d);var v=require(vehicleRepository,d.getVehicleId());DriverMapper.apply(request,d);d.setVehicleId(v.getId());if(password!=null&&!password.isBlank()){check(password.length()>=8,"Le mot de passe doit contenir au moins 8 caractères");d.setPasswordHash(passwordEncoder.encode(password));}VehicleMapper.apply(vehicleRequest,v);validateVehicle(v);validateDriver(d);vehicleRepository.saveAndFlush(v);driverRepository.saveAndFlush(d);audit("Driver",id,"Modification du chauffeur et du véhicule");return DriverMapper.response(d);
    }
    @Override @Transactional(readOnly=true) public VehicleResponse getVehicleByDriver(Long driverId){var d=require(driverRepository,driverId);return getVehicle(d.getVehicleId());}
    @Override public RideResponse acceptRide(Long rideId,Long driverId){lock();var r=require(rideRepository,rideId);var d=require(driverRepository,driverId);check("EN_ATTENTE".equals(r.getStatus()),"Cette demande a déjà été acceptée");check(!d.getLicenseExpiry().isBefore(LocalDate.now()),"Permis expiré");var v=require(vehicleRepository,d.getVehicleId());check(v.getCapacity()>=r.getPassengers(),"Nombre de passagers supérieur aux places disponibles");check(rideRepository.findAll().stream().noneMatch(x->active(x)&&Objects.equals(x.getDriverId(),driverId)),"Vous avez déjà une course active");r.setDriverId(driverId);r.setVehicleId(v.getId());r.setStatus("ASSIGNEE");d.setStatus("EN_COURSE");v.setStatus("EN_COURSE");audit("Ride",rideId,"Acceptée par "+d.getName());return RideMapper.response(r);}

    @Override @Transactional(readOnly=true)
    public PageResponse<RideResponse> listRide(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","clientId","departure","destination","distance","scheduledAt","passengers","fare","driverId","vehicleId","status","startedAt","completedAt");
        return PageResponse.of(rideRepository.findAll(spec(q,status,filter,List.of("departure","destination","status"),allowed),safePage(pageable,allowed)).map(RideMapper::response));
    }
    @Override @Transactional(readOnly=true) public RideResponse getRide(Long id) {return RideMapper.response(require(rideRepository,id));}
    private void validateRide(Ride e) {
        if(e.getClientId()!=null) require(clientRepository,e.getClientId());
        validate(e);
    }
    @Override public RideResponse createRide(RideRequest request) {
        lock(); var e=new Ride(); RideMapper.apply(request,e); validateRide(e);
        rideRepository.saveAndFlush(e); afterCreate(e); audit("Ride",e.getId(),"Création"); return RideMapper.response(e);
    }
    @Override public RideResponse updateRide(Long id,RideRequest request) {
        lock(); var e=require(rideRepository,id); beforeUpdate(e); RideMapper.apply(request,e); validateRide(e);
        rideRepository.saveAndFlush(e); audit("Ride",id,"Modification"); return RideMapper.response(e);
    }
    @Override public void deleteRide(Long id) {
        lock(); var e=require(rideRepository,id); beforeDelete(e);
        rideRepository.delete(e); audit("Ride",id,"Suppression");
    }

    public static final Map<String,Double> DEFAULTS=Map.of("fare.base",5.0,"fare.km",2.0,"commission",0.1,"score.distance",1.0,"score.workload",2.0);
    private boolean active(Ride r){return Set.of("ASSIGNEE","EN_COURS").contains(r.getStatus());}
    private void validate(Object obj){
        if(obj instanceof Vehicle v){check(v.getYear()>=1950 && v.getYear()<=LocalDate.now().getYear()+1,"Année invalide");check(rideRepository.findAll().stream().noneMatch(r->active(r)&&Objects.equals(r.getVehicleId(),v.getId())),"Véhicule affecté à une course");}
        if(obj instanceof Driver d){check(!d.getLicenseExpiry().isBefore(d.getHiredAt()),"Dates du permis incohérentes");conflict(driverRepository.findAll().stream().noneMatch(v->!Objects.equals(v.getId(),d.getId())&&v.getVehicleId().equals(d.getVehicleId())),"Véhicule déjà associé à un chauffeur");}
        if(obj instanceof Ride r){check("EN_ATTENTE".equals(r.getStatus()),"Course déjà attribuée");r.setFare(money(cfg("fare.base")+r.getDistance().doubleValue()*cfg("fare.km")));}
    }
    private void beforeUpdate(Object obj){
        if(obj instanceof Ride r)check("EN_ATTENTE".equals(r.getStatus()),"Une course attribuée ne peut plus être modifiée");
        if(obj instanceof Driver d)check(rideRepository.findAll().stream().noneMatch(r->active(r)&&Objects.equals(r.getDriverId(),d.getId())),"Chauffeur affecté à une course");
        if(obj instanceof Vehicle v)check(rideRepository.findAll().stream().noneMatch(r->active(r)&&Objects.equals(r.getVehicleId(),v.getId())),"Véhicule affecté à une course");
    }
    private void beforeDelete(Object obj){beforeUpdate(obj);if(obj instanceof Ride r)check("EN_ATTENTE".equals(r.getStatus()),"Conservez l'historique des courses");if(obj instanceof Vehicle v)conflict(rideRepository.findAll().stream().noneMatch(r->Objects.equals(r.getVehicleId(),v.getId())),"Véhicule utilisé dans l’historique");if(obj instanceof Driver d)conflict(rideRepository.findAll().stream().noneMatch(r->Objects.equals(r.getDriverId(),d.getId())),"Chauffeur utilisé dans l'historique");}
    private void afterCreate(Object obj){}
    private double score(Driver d){long count=rideRepository.findAll().stream().filter(r->Objects.equals(r.getDriverId(),d.getId())&&"TERMINEE".equals(r.getStatus())&&r.getCompletedAt().toLocalDate().equals(LocalDate.now())).count();return d.getDistanceToPickup().doubleValue()*cfg("score.distance")+count*cfg("score.workload");}
    private void assign(Ride r){check("EN_ATTENTE".equals(r.getStatus()),"Course déjà attribuée");
        var d=driverRepository.findAll().stream().filter(v->"DISPONIBLE".equals(v.getStatus())&&!v.getLicenseExpiry().isBefore(r.getScheduledAt().toLocalDate())&&!v.getLicenseExpiry().isBefore(LocalDate.now())).filter(v->{var car=require(vehicleRepository,v.getVehicleId());return "DISPONIBLE".equals(car.getStatus())&&car.getCapacity()>=r.getPassengers()&&rideRepository.findAll().stream().noneMatch(x->active(x)&&(Objects.equals(x.getDriverId(),v.getId())||Objects.equals(x.getVehicleId(),car.getId())));}).min(Comparator.comparingDouble(this::score).thenComparing(Driver::getId)).orElseThrow(()->new ConflictException("Aucun chauffeur éligible"));
        var car=require(vehicleRepository,d.getVehicleId());d.setStatus("EN_COURSE");car.setStatus("EN_COURSE");r.setDriverId(d.getId());r.setVehicleId(car.getId());r.setStatus("ASSIGNEE");audit("Ride",r.getId(),"Attribution : "+d.getName());
    }
    private void release(Ride r){if(r.getDriverId()!=null)require(driverRepository,r.getDriverId()).setStatus("DISPONIBLE");if(r.getVehicleId()!=null)require(vehicleRepository,r.getVehicleId()).setStatus("DISPONIBLE");}
    @Override public Object perform(String resource,Long id,String operation,ActionRequest request){lock();var r=require(rideRepository,id);
        switch(operation){case "assign"->assign(r);case "start"->{check("ASSIGNEE".equals(r.getStatus()),"Attribuez la course avant le départ");check(!require(driverRepository,r.getDriverId()).getLicenseExpiry().isBefore(LocalDate.now()),"Permis expiré");r.setStatus("EN_COURS");r.setStartedAt(LocalDateTime.now());audit("Ride",id,"Départ");}
        case "complete"->{check("EN_COURS".equals(r.getStatus()),"La course n'a pas démarré");r.setStatus("TERMINEE");r.setCompletedAt(LocalDateTime.now());var car=require(vehicleRepository,r.getVehicleId());car.setMileage(car.getMileage().add(r.getDistance()));release(r);audit("Ride",id,"Course terminée");}
        case "cancel"->{check(!Set.of("TERMINEE","ANNULEE").contains(r.getStatus()),"Course clôturée");r.setStatus("ANNULEE");release(r);audit("Ride",id,"Annulation");}
        default->throw new InvalidOperationException("Action inconnue");}return RideMapper.response(r);
    }
    @Override @Transactional(readOnly=true) public AnalyticsResponse statistics(LocalDate from,LocalDate to,Long actorId,String zone){
        dates(from,to);var all=rideRepository.findAll().stream().filter(r->between(r.getScheduledAt().toLocalDate(),from,to)&&(actorId==null||actorId.equals(r.getDriverId()))).toList();var done=all.stream().filter(r->"TERMINEE".equals(r.getStatus())).toList();double total=done.stream().mapToDouble(r->r.getFare().doubleValue()).sum();var metrics=new LinkedHashMap<String,Number>();metrics.put("Courses",all.size());metrics.put("Chiffre d'affaires",money(total));metrics.put("Distance (km)",done.stream().mapToDouble(r->r.getDistance().doubleValue()).sum());metrics.put("Annulation (%)",all.isEmpty()?0:100.0*all.stream().filter(r->"ANNULEE".equals(r.getStatus())).count()/all.size());metrics.put("Revenu moyen",done.isEmpty()?0:money(total/done.size()));metrics.put("Commission",money(total*Math.min(1,cfg("commission"))));
        var groups=new LinkedHashMap<String,Map<String,Number>>();for(var d:driverRepository.findAll()){var rows=done.stream().filter(r->Objects.equals(r.getDriverId(),d.getId())).toList();double revenue=rows.stream().mapToDouble(r->r.getFare().doubleValue()).sum();groups.put("Chauffeur "+d.getId()+" · "+d.getName(),Map.of("Courses",rows.size(),"Revenu",money(revenue*(1-Math.min(1,cfg("commission"))))));}
        for(var v:vehicleRepository.findAll()){var rows=done.stream().filter(r->Objects.equals(r.getVehicleId(),v.getId())).toList();groups.put("Véhicule "+v.getPlate(),Map.of("Courses",rows.size(),"Revenu",money(rows.stream().mapToDouble(r->r.getFare().doubleValue()).sum())));}
        for(var day:done.stream().map(r->r.getCompletedAt().toLocalDate().toString()).distinct().toList())groups.put("Jour "+day,Map.of("Revenu",money(done.stream().filter(r->r.getCompletedAt().toLocalDate().toString().equals(day)).mapToDouble(r->r.getFare().doubleValue()).sum())));
        for(var month:done.stream().map(r->YearMonth.from(r.getCompletedAt()).toString()).distinct().toList())groups.put("Mois "+month,Map.of("Revenu",money(done.stream().filter(r->YearMonth.from(r.getCompletedAt()).toString().equals(month)).mapToDouble(r->r.getFare().doubleValue()).sum())));
        metrics.put("Chauffeur le plus actif (ID)",driverRepository.findAll().stream().max(Comparator.comparingLong(d->done.stream().filter(r->Objects.equals(r.getDriverId(),d.getId())).count())).filter(d->done.stream().anyMatch(r->Objects.equals(r.getDriverId(),d.getId()))).map(Driver::getId).orElse(0L));
        metrics.put("Véhicule le plus utilisé (ID)",vehicleRepository.findAll().stream().max(Comparator.comparingLong(v->done.stream().filter(r->Objects.equals(r.getVehicleId(),v.getId())).count())).filter(v->done.stream().anyMatch(r->Objects.equals(r.getVehicleId(),v.getId()))).map(Vehicle::getId).orElse(0L));return new AnalyticsResponse(metrics,groups);
    }
    @Override @Transactional(readOnly=true) public void detail(String resource,Long id,Model m){if("Driver".equals(resource))m.addAttribute("rides",rideRepository.findAll().stream().filter(r->Objects.equals(r.getDriverId(),id)).map(RideMapper::response).toList());if("Ride".equals(resource)){var r=require(rideRepository,id);if(r.getDriverId()!=null)m.addAttribute("driver",getDriver(r.getDriverId()));if(r.getVehicleId()!=null)m.addAttribute("vehicle",getVehicle(r.getVehicleId()));}}

}
