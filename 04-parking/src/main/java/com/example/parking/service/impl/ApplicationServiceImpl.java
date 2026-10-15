package com.example.parking.service.impl;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.parking.service.ApplicationService;
import com.example.parking.entity.*;
import com.example.parking.repository.*;
import com.example.parking.mapper.*;
import com.example.parking.dto.request.*;
import com.example.parking.dto.response.*;
import com.example.parking.exception.*;
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
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingStayRepository parkingStayRepository;

    private final SystemStateRepository state;
    private final AuditEventRepository auditRepository;
    private final SettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;
    public ApplicationServiceImpl(CustomerRepository customerRepository, VehicleRepository vehicleRepository, ParkingSpaceRepository parkingSpaceRepository, SubscriptionRepository subscriptionRepository, ReservationRepository reservationRepository, ParkingStayRepository parkingStayRepository,SystemStateRepository state,AuditEventRepository auditRepository,SettingRepository settingRepository,PasswordEncoder passwordEncoder) {
        this.customerRepository=customerRepository;
        this.vehicleRepository=vehicleRepository;
        this.parkingSpaceRepository=parkingSpaceRepository;
        this.subscriptionRepository=subscriptionRepository;
        this.reservationRepository=reservationRepository;
        this.parkingStayRepository=parkingStayRepository;

        this.state=state; this.auditRepository=auditRepository; this.settingRepository=settingRepository; this.passwordEncoder=passwordEncoder;
    }
    private String settingLabel(String name){return switch(name){
        case "rate.hour" -> "Tarif horaire";
        case "rate.minimum" -> "Montant minimum";
        case "rate.daily" -> "Forfait journalier";
        case "rate.vip" -> "Coefficient VIP";
        case "rate.subscriber" -> "Réduction abonné (0 à 1)";
        case "rate.overrun" -> "Coefficient de dépassement";
        default -> name.startsWith("rate.zone.") ? "Zone "+name.substring(10)+" : coefficient" : name.startsWith("price.zone.") ? "Zone "+name.substring(11)+" : coefficient" : name.startsWith("rate.vehicle.") ? "Véhicule "+name.substring(13)+" : coefficient" : name;
    };}
    private void lock(){state.lock().orElseThrow(()->new InvalidOperationException("Initialisation manquante"));}
    private <E> E require(JpaRepository<E,Long> repository,Long id){if(id==null) throw new InvalidOperationException("Identifiant obligatoire");return repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Enregistrement introuvable : "+id));}
    private void check(boolean valid,String message){if(!valid)throw new InvalidOperationException(message);}
    private void conflict(boolean valid,String message){if(!valid)throw new ConflictException(message);}
    private void audit(String resource,Long id,String action){var auth=SecurityContextHolder.getContext().getAuthentication();auditRepository.save(new AuditEvent(resource,id,action,auth==null ? "system" : auth.getName()));}
    @Override @Transactional(readOnly=true) public List<AuditResponse> history(String resource,Long id){return auditRepository.findByResourceAndResourceIdOrderByOccurredAtDesc(resource,id).stream().map(e->new AuditResponse(e.getAction(),e.getActor(),e.getOccurredAt())).toList();}
    @Override @Transactional(readOnly=true) public List<SettingResponse> settings(){return settingRepository.findAll().stream().sorted(Comparator.comparing(Setting::getName)).map(s->new SettingResponse(s.getName(),settingLabel(s.getName()),s.getValue())).toList();}
    @Override public void setting(SettingRequest r){lock();check(DEFAULTS.containsKey(r.name()) || r.name().startsWith("rate.zone."),"Paramètre inconnu");check(!Set.of("commission","rate.subscriber").contains(r.name()) || r.value()<=1,"Ce taux doit être compris entre 0 et 1");check(!r.name().startsWith("points.") || r.value()==Math.floor(r.value()),"Les points doivent être entiers");settingRepository.save(new Setting(r.name(),r.value()));audit("Setting",0L,r.name()+" = "+r.value());}
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
    public PageResponse<CustomerResponse> listCustomer(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","name","email","phone","type");
        return PageResponse.of(customerRepository.findAll(spec(q,status,filter,List.of("name","email","phone","type"),allowed),safePage(pageable,allowed)).map(CustomerMapper::response));
    }
    @Override @Transactional(readOnly=true) public CustomerResponse getCustomer(Long id) {return CustomerMapper.response(require(customerRepository,id));}
    @Override @Transactional(readOnly=true) public CustomerResponse getCustomerByEmail(String email) {
        return customerRepository.findByEmailIgnoreCase(email).map(CustomerMapper::response)
            .orElseThrow(()->new ResourceNotFoundException("Compte client introuvable"));
    }
    private void validateCustomer(Customer e) {
        if(e.getType()==null || e.getType().isBlank()) e.setType("STANDARD");
        e.setEmail(e.getEmail().trim().toLowerCase(Locale.ROOT));
        if(customerRepository.findByEmailIgnoreCase(e.getEmail()).filter(v->!Objects.equals(v.getId(),e.getId())).isPresent()) throw new ConflictException("Cette adresse e-mail est déjà utilisée");
        validate(e);
    }
    @Override public CustomerResponse createCustomer(CustomerRequest request) {
        lock(); var e=new Customer(); CustomerMapper.apply(request,e); validateCustomer(e);
        customerRepository.saveAndFlush(e); afterCreate(e); audit("Customer",e.getId(),"Création"); return CustomerMapper.response(e);
    }
    @Override public CustomerResponse updateCustomer(Long id,CustomerRequest request) {
        lock(); var e=require(customerRepository,id); beforeUpdate(e); CustomerMapper.apply(request,e); validateCustomer(e);
        customerRepository.saveAndFlush(e); audit("Customer",id,"Modification"); return CustomerMapper.response(e);
    }
    @Override public void deleteCustomer(Long id) {
        lock(); var e=require(customerRepository,id); beforeDelete(e);
        if(vehicleRepository.findAll().stream().anyMatch(v->Objects.equals(v.getCustomerId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Véhicules");
        if(subscriptionRepository.findAll().stream().anyMatch(v->Objects.equals(v.getCustomerId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Abonnements");
        if(reservationRepository.findAll().stream().anyMatch(v->Objects.equals(v.getCustomerId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Réservations");
        if(parkingStayRepository.findAll().stream().anyMatch(v->Objects.equals(v.getCustomerId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Entrées et sorties");
        customerRepository.delete(e); audit("Customer",id,"Suppression");
    }
    @Override public void setCustomerPassword(Long id,String rawPassword) {
        lock(); check(rawPassword!=null && rawPassword.length()>=8,"Le mot de passe doit contenir au moins 8 caractères");
        var customer=require(customerRepository,id); customer.setPasswordHash(passwordEncoder.encode(rawPassword));
        customerRepository.saveAndFlush(customer); audit("Customer",id,"Mot de passe défini");
    }

    @Override public CustomerResponse createCustomerWithVehicle(CustomerRequest request,String rawPassword,String plate,String vehicleType,String brand,String model) {
        lock();
        check(rawPassword!=null && rawPassword.length()>=8,"Le mot de passe doit contenir au moins 8 caractères");
        var customer=new Customer(); CustomerMapper.apply(request,customer); validateCustomer(customer);
        customer.setPasswordHash(passwordEncoder.encode(rawPassword));
        customerRepository.saveAndFlush(customer);
        var vehicle=new Vehicle();
        applyRegistrationVehicle(vehicle,customer.getId(),plate,vehicleType,brand,model);
        validateVehicle(vehicle); vehicleRepository.saveAndFlush(vehicle);
        afterCreate(customer); afterCreate(vehicle);
        audit("Customer",customer.getId(),"Création du compte avec véhicule");
        audit("Vehicle",vehicle.getId(),"Création avec le compte client");
        return CustomerMapper.response(customer);
    }

    @Override public CustomerResponse updateCustomerWithVehicle(Long id,CustomerRequest request,String rawPassword,String plate,String vehicleType,String brand,String model) {
        lock();
        var customer=require(customerRepository,id); beforeUpdate(customer); CustomerMapper.apply(request,customer); validateCustomer(customer);
        if(rawPassword!=null && !rawPassword.isBlank()) {
            check(rawPassword.length()>=8,"Le mot de passe doit contenir au moins 8 caractères");
            customer.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        customerRepository.saveAndFlush(customer);
        var vehicle=vehicleRepository.findFirstByCustomerId(id).orElseGet(Vehicle::new);
        boolean created=vehicle.getId()==null;
        applyRegistrationVehicle(vehicle,id,plate,vehicleType,brand,model);
        validateVehicle(vehicle); vehicleRepository.saveAndFlush(vehicle);
        if(created) afterCreate(vehicle);
        audit("Customer",id,"Modification du compte et du véhicule");
        audit("Vehicle",vehicle.getId(),created?"Création avec le compte client":"Modification depuis le compte client");
        return CustomerMapper.response(customer);
    }

    private void applyRegistrationVehicle(Vehicle vehicle,Long customerId,String plate,String vehicleType,String brand,String model) {
        check(plate!=null && !plate.isBlank(),"L'immatriculation est obligatoire");
        check(vehicleType!=null && Set.of("VOITURE","MOTO","UTILITAIRE").contains(vehicleType),"Type de véhicule invalide");
        check(brand!=null && !brand.isBlank(),"La marque est obligatoire");
        check(model!=null && !model.isBlank(),"Le modèle est obligatoire");
        vehicle.setCustomerId(customerId);
        vehicle.setPlate(plate.trim().toUpperCase(Locale.ROOT));
        vehicle.setType(vehicleType);
        vehicle.setBrand(brand.trim());
        vehicle.setModel(model.trim());
    }

    @Override @Transactional(readOnly=true) public VehicleResponse getVehicleByCustomer(Long customerId) {
        return vehicleRepository.findFirstByCustomerId(customerId).map(VehicleMapper::response)
            .orElseThrow(()->new ResourceNotFoundException("Aucun véhicule associé à ce client"));
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<VehicleResponse> listVehicle(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","plate","customerId","type");
        return PageResponse.of(vehicleRepository.findAll(spec(q,status,filter,List.of("plate","type"),allowed),safePage(pageable,allowed)).map(VehicleMapper::response));
    }
    @Override @Transactional(readOnly=true) public VehicleResponse getVehicle(Long id) {return VehicleMapper.response(require(vehicleRepository,id));}
    private void validateVehicle(Vehicle e) {
        if(e.getCustomerId()!=null) require(customerRepository,e.getCustomerId());
        if(e.getType()==null || e.getType().isBlank()) e.setType("VOITURE");
        e.setPlate(e.getPlate().trim().toUpperCase(Locale.ROOT));
        if(vehicleRepository.findByPlateIgnoreCase(e.getPlate()).filter(v->!Objects.equals(v.getId(),e.getId())).isPresent()) throw new ConflictException("Cette immatriculation est déjà utilisée");
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
        if(subscriptionRepository.findAll().stream().anyMatch(v->Objects.equals(v.getVehicleId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Abonnements");
        if(reservationRepository.findAll().stream().anyMatch(v->Objects.equals(v.getVehicleId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Réservations");
        if(parkingStayRepository.findAll().stream().anyMatch(v->Objects.equals(v.getVehicleId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Entrées et sorties");
        vehicleRepository.delete(e); audit("Vehicle",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<ParkingSpaceResponse> listParkingSpace(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","number","zone","type","status");
        return PageResponse.of(parkingSpaceRepository.findAll(spec(q,status,filter,List.of("number","zone","type","status"),allowed),safePage(pageable,allowed)).map(ParkingSpaceMapper::response));
    }
    @Override @Transactional(readOnly=true) public ParkingSpaceResponse getParkingSpace(Long id) {return ParkingSpaceMapper.response(require(parkingSpaceRepository,id));}
    private void validateParkingSpace(ParkingSpace e) {
        if(e.getType()==null || e.getType().isBlank()) e.setType("STANDARD");
        if(e.getStatus()==null || e.getStatus().isBlank()) e.setStatus("LIBRE");
        if(parkingSpaceRepository.findAll().stream().anyMatch(v->!Objects.equals(v.getId(),e.getId()) && Objects.equals(v.getNumber(),e.getNumber()))) throw new ConflictException("Numéro déjà utilisé");
        validate(e);
    }
    @Override public ParkingSpaceResponse createParkingSpace(ParkingSpaceRequest request) {
        lock(); var e=new ParkingSpace(); ParkingSpaceMapper.apply(request,e); validateParkingSpace(e);
        parkingSpaceRepository.saveAndFlush(e); afterCreate(e); audit("ParkingSpace",e.getId(),"Création"); return ParkingSpaceMapper.response(e);
    }
    @Override public ParkingSpaceResponse updateParkingSpace(Long id,ParkingSpaceRequest request) {
        lock(); var e=require(parkingSpaceRepository,id); beforeUpdate(e); ParkingSpaceMapper.apply(request,e); validateParkingSpace(e);
        parkingSpaceRepository.saveAndFlush(e); audit("ParkingSpace",id,"Modification"); return ParkingSpaceMapper.response(e);
    }
    @Override public void deleteParkingSpace(Long id) {
        lock(); var e=require(parkingSpaceRepository,id); beforeDelete(e);
        if(reservationRepository.findAll().stream().anyMatch(v->Objects.equals(v.getSpaceId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Réservations");
        if(parkingStayRepository.findAll().stream().anyMatch(v->Objects.equals(v.getSpaceId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Entrées et sorties");
        parkingSpaceRepository.delete(e); audit("ParkingSpace",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<SubscriptionResponse> listSubscription(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","customerId","vehicleId","type","startDate","endDate","status");
        return PageResponse.of(subscriptionRepository.findAll(spec(q,status,filter,List.of("type","status"),allowed),safePage(pageable,allowed)).map(SubscriptionMapper::response));
    }
    @Override @Transactional(readOnly=true) public SubscriptionResponse getSubscription(Long id) {return SubscriptionMapper.response(require(subscriptionRepository,id));}
    private void validateSubscription(Subscription e) {
        if(e.getCustomerId()!=null) require(customerRepository,e.getCustomerId());
        if(e.getVehicleId()!=null) require(vehicleRepository,e.getVehicleId());
        if(e.getType()==null || e.getType().isBlank()) e.setType("JOURNALIER");
        if(e.getStatus()==null || e.getStatus().isBlank()) e.setStatus("ACTIF");
        validate(e);
    }
    @Override public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        lock(); var e=new Subscription(); SubscriptionMapper.apply(request,e); validateSubscription(e);
        subscriptionRepository.saveAndFlush(e); afterCreate(e); audit("Subscription",e.getId(),"Création"); return SubscriptionMapper.response(e);
    }
    @Override public SubscriptionResponse updateSubscription(Long id,SubscriptionRequest request) {
        lock(); var e=require(subscriptionRepository,id); beforeUpdate(e); SubscriptionMapper.apply(request,e); validateSubscription(e);
        subscriptionRepository.saveAndFlush(e); audit("Subscription",id,"Modification"); return SubscriptionMapper.response(e);
    }
    @Override public void deleteSubscription(Long id) {
        lock(); var e=require(subscriptionRepository,id); beforeDelete(e);
        subscriptionRepository.delete(e); audit("Subscription",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<ReservationResponse> listReservation(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","customerId","vehicleId","spaceId","arrival","departure","status");
        return PageResponse.of(reservationRepository.findAll(spec(q,status,filter,List.of("status"),allowed),safePage(pageable,allowed)).map(ReservationMapper::response));
    }
    @Override @Transactional(readOnly=true) public ReservationResponse getReservation(Long id) {return ReservationMapper.response(require(reservationRepository,id));}
    private void validateReservation(Reservation e) {
        if(e.getCustomerId()!=null) require(customerRepository,e.getCustomerId());
        e.setVehicleId(assignedVehicleId(e.getCustomerId()));
        if(e.getSpaceId()!=null) require(parkingSpaceRepository,e.getSpaceId());
        validate(e);
    }
    @Override public ReservationResponse createReservation(ReservationRequest request) {
        lock(); var e=new Reservation(); ReservationMapper.apply(request,e); validateReservation(e);
        reservationRepository.saveAndFlush(e); afterCreate(e); audit("Reservation",e.getId(),"Création"); return ReservationMapper.response(e);
    }
    @Override public ReservationResponse updateReservation(Long id,ReservationRequest request) {
        lock(); var e=require(reservationRepository,id); beforeUpdate(e); ReservationMapper.apply(request,e); validateReservation(e);
        reservationRepository.saveAndFlush(e); audit("Reservation",id,"Modification"); return ReservationMapper.response(e);
    }
    @Override public void deleteReservation(Long id) {
        lock(); var e=require(reservationRepository,id); beforeDelete(e);
        if(parkingStayRepository.findAll().stream().anyMatch(v->Objects.equals(v.getReservationId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Entrées et sorties");
        reservationRepository.delete(e); audit("Reservation",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<ParkingStayResponse> listParkingStay(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","customerId","vehicleId","spaceId","reservationId","enteredAt","expectedExit","exitedAt","amount","status");
        return PageResponse.of(parkingStayRepository.findAll(spec(q,status,filter,List.of("status"),allowed),safePage(pageable,allowed)).map(ParkingStayMapper::response));
    }
    @Override @Transactional(readOnly=true) public ParkingStayResponse getParkingStay(Long id) {return ParkingStayMapper.response(require(parkingStayRepository,id));}
    private void validateParkingStay(ParkingStay e) {
        if(e.getCustomerId()!=null) require(customerRepository,e.getCustomerId());
        e.setVehicleId(assignedVehicleId(e.getCustomerId()));
        if(e.getSpaceId()!=null) require(parkingSpaceRepository,e.getSpaceId());
        if(e.getReservationId()!=null) require(reservationRepository,e.getReservationId());
        validate(e);
    }
    @Override public ParkingStayResponse createParkingStay(ParkingStayRequest request) {
        lock(); var e=new ParkingStay(); ParkingStayMapper.apply(request,e); validateParkingStay(e);
        parkingStayRepository.saveAndFlush(e); afterCreate(e); audit("ParkingStay",e.getId(),"Création"); return ParkingStayMapper.response(e);
    }
    @Override public ParkingStayResponse updateParkingStay(Long id,ParkingStayRequest request) {
        lock(); var e=require(parkingStayRepository,id); beforeUpdate(e); ParkingStayMapper.apply(request,e); validateParkingStay(e);
        parkingStayRepository.saveAndFlush(e); audit("ParkingStay",id,"Modification"); return ParkingStayMapper.response(e);
    }
    @Override public void deleteParkingStay(Long id) {
        lock(); var e=require(parkingStayRepository,id); beforeDelete(e);
        parkingStayRepository.delete(e); audit("ParkingStay",id,"Suppression");
    }

    public static final Map<String,Double> DEFAULTS=Map.ofEntries(Map.entry("rate.hour",2.0),Map.entry("rate.minimum",2.0),Map.entry("rate.daily",20.0),Map.entry("rate.zone.A",1.0),Map.entry("rate.zone.B",1.2),Map.entry("rate.vehicle.VOITURE",1.0),Map.entry("rate.vehicle.MOTO",0.5),Map.entry("rate.vehicle.UTILITAIRE",1.5),Map.entry("rate.vip",0.9),Map.entry("rate.subscriber",0.3),Map.entry("rate.overrun",1.5));
    private boolean overlap(LocalDateTime a,LocalDateTime b,LocalDateTime c,LocalDateTime d){return a.isBefore(d)&&c.isBefore(b);}
    private List<ParkingStay> activeStays(){return parkingStayRepository.findAll().stream().filter(s->s.getExitedAt()==null).toList();}
    private boolean subscriber(Long customer,Long vehicle,LocalDate date){return subscriptionRepository.findAll().stream().anyMatch(s->s.getCustomerId().equals(customer)&&s.getVehicleId().equals(vehicle)&&"ACTIF".equals(s.getStatus())&&!date.isBefore(s.getStartDate())&&!date.isAfter(s.getEndDate()));}
    private List<String> preferences(Customer c,Long vehicle,LocalDate date){if("PMR".equals(c.getType()))return List.of("PMR","STANDARD");if("VIP".equals(c.getType()))return List.of("VIP","STANDARD");if(subscriber(c.getId(),vehicle,date))return List.of("ABONNE","STANDARD");return List.of("STANDARD");}
    private boolean free(Long space,LocalDateTime from,LocalDateTime to,Long ignoreReservation){
        if("HORS_SERVICE".equals(require(parkingSpaceRepository,space).getStatus()))return false;
        if(reservationRepository.findAll().stream().anyMatch(r->"CONFIRMEE".equals(r.getStatus())&&!Objects.equals(r.getId(),ignoreReservation)&&r.getSpaceId().equals(space)&&overlap(from,to,r.getArrival(),r.getDeparture())))return false;
        return activeStays().stream().noneMatch(s->s.getSpaceId().equals(space)&&overlap(from,to,s.getEnteredAt(),s.getExpectedExit().isAfter(LocalDateTime.now())?s.getExpectedExit():LocalDateTime.MAX));
    }
    private Long choose(Customer c,Long vehicle,Long selected,LocalDateTime from,LocalDateTime to,Long ignore){
        var types=preferences(c,vehicle,from.toLocalDate());
        if(selected!=null){var s=require(parkingSpaceRepository,selected);check(types.contains(s.getType()),"Place incompatible avec la catégorie de l'usager");conflict(free(selected,from,to,ignore),"Place indisponible sur ce créneau");return selected;}
        return parkingSpaceRepository.findAll().stream().filter(s->types.contains(s.getType())&&free(s.getId(),from,to,ignore)).min(Comparator.comparingInt((ParkingSpace s)->types.indexOf(s.getType())).thenComparing(ParkingSpace::getNumber)).map(ParkingSpace::getId).orElseThrow(()->new ConflictException("Aucune place compatible disponible"));
    }
    private void owner(Long customer,Long vehicle){check(require(vehicleRepository,vehicle).getCustomerId().equals(customer),"Ce véhicule n'appartient pas à l'usager");}
    private Long assignedVehicleId(Long customerId){
        check(customerId!=null,"Usager obligatoire");
        return vehicleRepository.findFirstByCustomerId(customerId)
            .orElseThrow(()->new InvalidOperationException("Ce client ne possède pas de voiture associée")).getId();
    }
    private void validate(Object obj){
        if(obj instanceof ParkingSpace s){if(s.getId()!=null){conflict(activeStays().stream().noneMatch(v->v.getSpaceId().equals(s.getId())),"Place occupée");conflict(reservationRepository.findAll().stream().noneMatch(r->r.getSpaceId().equals(s.getId())&&"CONFIRMEE".equals(r.getStatus())&&r.getDeparture().isAfter(LocalDateTime.now())),"Place réservée");}}
        if(obj instanceof Subscription s){owner(s.getCustomerId(),s.getVehicleId());check(!s.getEndDate().isBefore(s.getStartDate()),"Dates d'abonnement invalides");long expected=switch(s.getType()){case "JOURNALIER"->1;case "HEBDOMADAIRE"->7;case "MENSUEL"->ChronoUnit.DAYS.between(s.getStartDate(),s.getStartDate().plusMonths(1));case "ANNUEL"->ChronoUnit.DAYS.between(s.getStartDate(),s.getStartDate().plusYears(1));default->0;};check(ChronoUnit.DAYS.between(s.getStartDate(),s.getEndDate())+1==expected,"La période ne correspond pas à la formule");}
        if(obj instanceof Reservation r){owner(r.getCustomerId(),r.getVehicleId());check(r.getDeparture().isAfter(r.getArrival())&&r.getDeparture().isAfter(LocalDateTime.now()),"Créneau invalide ou expiré");conflict(reservationRepository.findAll().stream().noneMatch(v->!Objects.equals(v.getId(),r.getId())&&v.getVehicleId().equals(r.getVehicleId())&&"CONFIRMEE".equals(v.getStatus())&&overlap(r.getArrival(),r.getDeparture(),v.getArrival(),v.getDeparture())),"Le véhicule possède déjà une réservation sur ce créneau");r.setSpaceId(choose(require(customerRepository,r.getCustomerId()),r.getVehicleId(),r.getSpaceId(),r.getArrival(),r.getDeparture(),r.getId()));}
        if(obj instanceof ParkingStay s){owner(s.getCustomerId(),s.getVehicleId());check(s.getExpectedExit().isAfter(s.getEnteredAt())&&!s.getEnteredAt().isAfter(LocalDateTime.now()),"Heures d'entrée et sortie invalides");conflict(activeStays().stream().noneMatch(v->v.getVehicleId().equals(s.getVehicleId())),"Véhicule déjà présent");
            if(s.getReservationId()!=null){var r=require(reservationRepository,s.getReservationId());check("CONFIRMEE".equals(r.getStatus())&&r.getCustomerId().equals(s.getCustomerId())&&r.getVehicleId().equals(s.getVehicleId()),"Réservation incompatible");check(s.getEnteredAt().isBefore(r.getDeparture()),"Réservation expirée");check(s.getExpectedExit().equals(r.getDeparture()),"Utilisez la sortie prévue de la réservation");check(s.getSpaceId()==null||s.getSpaceId().equals(r.getSpaceId()),"Place différente de la réservation");s.setSpaceId(r.getSpaceId());}
            s.setSpaceId(choose(require(customerRepository,s.getCustomerId()),s.getVehicleId(),s.getSpaceId(),s.getEnteredAt(),s.getExpectedExit(),s.getReservationId()));
        }
    }
    private void beforeUpdate(Object obj){
        if(obj instanceof ParkingStay)throw new InvalidOperationException("Utilisez l'enregistrement de sortie");
        if(obj instanceof Reservation r)check("CONFIRMEE".equals(r.getStatus()),"Réservation déjà utilisée ou annulée");
        if(obj instanceof Customer c){conflict(activeStays().stream().noneMatch(s->s.getCustomerId().equals(c.getId())),"Usager actuellement dans le parking");conflict(reservationRepository.findAll().stream().noneMatch(r->r.getCustomerId().equals(c.getId())&&"CONFIRMEE".equals(r.getStatus())&&r.getDeparture().isAfter(LocalDateTime.now())),"Usager avec réservation active");}
        if(obj instanceof Vehicle v){conflict(activeStays().stream().noneMatch(s->s.getVehicleId().equals(v.getId())),"Véhicule actuellement dans le parking");conflict(reservationRepository.findAll().stream().noneMatch(r->r.getVehicleId().equals(v.getId())&&"CONFIRMEE".equals(r.getStatus())&&r.getDeparture().isAfter(LocalDateTime.now())),"Véhicule avec réservation active");conflict(subscriptionRepository.findAll().stream().noneMatch(s->s.getVehicleId().equals(v.getId())),"Véhicule associé à un abonnement");}
    }
    private void beforeDelete(Object obj){beforeUpdate(obj);if(obj instanceof Reservation || obj instanceof Subscription)throw new InvalidOperationException("Conservez l'historique; utilisez l'annulation");}
    private void refresh(){for(var s:parkingSpaceRepository.findAll())if(!"HORS_SERVICE".equals(s.getStatus())){boolean busy=activeStays().stream().anyMatch(v->v.getSpaceId().equals(s.getId()));boolean reserved=reservationRepository.findAll().stream().anyMatch(r->r.getSpaceId().equals(s.getId())&&"CONFIRMEE".equals(r.getStatus())&&!r.getArrival().isAfter(LocalDateTime.now())&&r.getDeparture().isAfter(LocalDateTime.now()));s.setStatus(busy?"OCCUPEE":reserved?"RESERVEE":"LIBRE");}}
    private void afterCreate(Object obj){if(obj instanceof ParkingStay s){if(s.getReservationId()!=null)require(reservationRepository,s.getReservationId()).setStatus("UTILISEE");audit("ParkingStay",s.getId(),"Entrée");}refresh();}
    private BigDecimal bill(ParkingStay s,LocalDateTime exit){
        var space=require(parkingSpaceRepository,s.getSpaceId());var customer=require(customerRepository,s.getCustomerId());var vehicle=require(vehicleRepository,s.getVehicleId());
        double hours=Math.max(1,Math.ceil(Duration.between(s.getEnteredAt(),exit).toMinutes()/60.0));double zone=settingRepository.findById("rate.zone."+space.getZone()).map(Setting::getValue).orElse(1.0);double factor=zone*cfg("rate.vehicle."+vehicle.getType())*("VIP".equals(customer.getType())?cfg("rate.vip"):1);double hourly=cfg("rate.hour")*factor,daily=cfg("rate.daily")*factor;
        double amount=Math.floor(hours/24)*daily+Math.min(hours%24*hourly,daily);amount=Math.max(cfg("rate.minimum"),amount);
        if(subscriber(s.getCustomerId(),s.getVehicleId(),s.getEnteredAt().toLocalDate()))amount*=1-Math.min(1,cfg("rate.subscriber"));
        if(exit.isAfter(s.getExpectedExit())){double excess=Math.ceil(Duration.between(s.getExpectedExit(),exit).toMinutes()/60.0);amount+=excess*hourly*Math.max(0,cfg("rate.overrun")-1);}
        return money(amount);
    }
    @Override public Object perform(String resource,Long id,String operation,ActionRequest r){lock();
        if("Reservation".equals(resource)){var v=require(reservationRepository,id);check("CONFIRMEE".equals(v.getStatus()),"Réservation déjà utilisée ou annulée");v.setStatus("ANNULEE");refresh();audit(resource,id,"Annulation");return ReservationMapper.response(v);}
        if("Subscription".equals(resource)){var v=require(subscriptionRepository,id);v.setStatus("ANNULE");audit(resource,id,"Annulation");return SubscriptionMapper.response(v);}
        var s=require(parkingStayRepository,id);check(s.getExitedAt()==null,"Sortie déjà enregistrée");LocalDateTime exit=r.at()==null?LocalDateTime.now():r.at();check(!exit.isBefore(s.getEnteredAt())&&!exit.isAfter(LocalDateTime.now()),"Heure de sortie incohérente");s.setAmount(bill(s,exit));s.setExitedAt(exit);s.setStatus("TERMINE");refresh();audit("ParkingStay",id,"Sortie : "+s.getAmount());return ParkingStayMapper.response(s);
    }
    @Override @Transactional(readOnly=true) public AnalyticsResponse statistics(LocalDate from,LocalDate to,Long actorId,String zone){dates(from,to);var all=parkingStayRepository.findAll().stream().filter(s->between(s.getEnteredAt().toLocalDate(),from,to)&&(actorId==null||actorId.equals(s.getCustomerId()))&&(zone==null||zone.isBlank()||zone.equals(require(parkingSpaceRepository,s.getSpaceId()).getZone()))).toList();var spaces=parkingSpaceRepository.findAll().stream().filter(s->zone==null||zone.isBlank()||zone.equals(s.getZone())).toList();long occupied=activeStays().stream().filter(s->spaces.stream().anyMatch(v->v.getId().equals(s.getSpaceId()))).count();var metrics=new LinkedHashMap<String,Number>();metrics.put("Places",spaces.size());metrics.put("Occupées",occupied);metrics.put("Disponibles",spaces.stream().filter(s->free(s.getId(),LocalDateTime.now(),LocalDateTime.now().plusHours(1),null)).count());metrics.put("Occupation (%)",spaces.isEmpty()?0:100.0*occupied/spaces.size());metrics.put("Entrées",all.size());metrics.put("Recettes",money(all.stream().filter(s->s.getAmount()!=null).mapToDouble(s->s.getAmount().doubleValue()).sum()));metrics.put("Abonnements valides",subscriptionRepository.findAll().stream().filter(s->"ACTIF".equals(s.getStatus())&&!LocalDate.now().isBefore(s.getStartDate())&&!LocalDate.now().isAfter(s.getEndDate())).count());var groups=new LinkedHashMap<String,Map<String,Number>>();for(var z:spaces.stream().map(ParkingSpace::getZone).distinct().toList())groups.put("Zone "+z,Map.of("Places",spaces.stream().filter(s->s.getZone().equals(z)).count(),"Occupées",activeStays().stream().filter(s->require(parkingSpaceRepository,s.getSpaceId()).getZone().equals(z)).count()));return new AnalyticsResponse(metrics,groups);}
    @Override @Transactional(readOnly=true) public void detail(String resource,Long id,Model m){if("ParkingStay".equals(resource)){var s=require(parkingStayRepository,id);m.addAttribute("estimate",s.getAmount()==null?bill(s,LocalDateTime.now()):s.getAmount());m.addAttribute("vehicle",getVehicle(s.getVehicleId()));m.addAttribute("customer",getCustomer(s.getCustomerId()));}if("Reservation".equals(resource)){var r=require(reservationRepository,id);m.addAttribute("vehicle",getVehicle(r.getVehicleId()));m.addAttribute("customer",getCustomer(r.getCustomerId()));}if("Subscription".equals(resource)){var s=require(subscriptionRepository,id);m.addAttribute("valid",subscriber(s.getCustomerId(),s.getVehicleId(),LocalDate.now()));}}


    @org.springframework.scheduling.annotation.Scheduled(cron="${app.refresh-cron:0 * * * * *}") @Transactional
    public void refreshAvailability(){lock();refresh();}
    @Override @Transactional(readOnly=true) public PageResponse<ParkingSpaceResponse> availableSpaces(LocalDateTime arrival,LocalDateTime departure,Long customerId,Long vehicleId,Pageable pageable){
        LocalDateTime from=arrival==null?LocalDateTime.now():arrival,to=departure==null?from.plusHours(1):departure;check(to.isAfter(from),"Créneau invalide");
        List<String> types=List.of("STANDARD","PMR","VIP","ABONNE");if(customerId!=null){check(vehicleId!=null,"Véhicule obligatoire");owner(customerId,vehicleId);types=preferences(require(customerRepository,customerId),vehicleId,from.toLocalDate());}
        List<String> accepted=types;var ids=parkingSpaceRepository.findAll().stream().filter(v->accepted.contains(v.getType())&&free(v.getId(),from,to,null)).map(ParkingSpace::getId).toList();
        return PageResponse.of(parkingSpaceRepository.findAll((root,query,cb)->root.get("id").in(ids),safePage(pageable,Set.of("id","number","zone","type","status"))).map(ParkingSpaceMapper::response));
    }
}
