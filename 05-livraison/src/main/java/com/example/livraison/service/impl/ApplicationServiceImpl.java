package com.example.livraison.service.impl;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.livraison.service.ApplicationService;
import com.example.livraison.entity.*;
import com.example.livraison.repository.*;
import com.example.livraison.mapper.*;
import com.example.livraison.dto.request.*;
import com.example.livraison.dto.response.*;
import com.example.livraison.exception.*;
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
    private final DelivererRepository delivererRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final ParcelRepository parcelRepository;
    private final IncidentRepository incidentRepository;
    private final DeliveryProofRepository deliveryProofRepository;

    private final SystemStateRepository state;
    private final AuditEventRepository auditRepository;
    private final SettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;
    public ApplicationServiceImpl(ClientRepository clientRepository, DelivererRepository delivererRepository, DeliveryOrderRepository deliveryOrderRepository, ParcelRepository parcelRepository, IncidentRepository incidentRepository, DeliveryProofRepository deliveryProofRepository,SystemStateRepository state,AuditEventRepository auditRepository,SettingRepository settingRepository,PasswordEncoder passwordEncoder) {
        this.clientRepository=clientRepository;
        this.delivererRepository=delivererRepository;
        this.deliveryOrderRepository=deliveryOrderRepository;
        this.parcelRepository=parcelRepository;
        this.incidentRepository=incidentRepository;
        this.deliveryProofRepository=deliveryProofRepository;

        this.state=state; this.auditRepository=auditRepository; this.settingRepository=settingRepository; this.passwordEncoder=passwordEncoder;
    }
    private String settingLabel(String name){return switch(name){
        case "score.distance" -> "Score : poids de la distance";
        case "price.base" -> "Tarif de base";
        case "price.km" -> "Prix par kilomètre";
        case "price.kg" -> "Prix par kilogramme";
        case "price.volume" -> "Prix par unité de volume";
        case "price.urgent" -> "Coefficient urgent";
        case "price.veryUrgent" -> "Coefficient très urgent";
        case "price.zone" -> "Coefficient de zone par défaut";
        case "score.load" -> "Score : poids de la charge";
        case "score.priority" -> "Score : poids de la priorité";
        default -> name.startsWith("rate.zone.") ? "Zone "+name.substring(10)+" : coefficient" : name.startsWith("price.zone.") ? "Zone "+name.substring(11)+" : coefficient" : name.startsWith("rate.vehicle.") ? "Véhicule "+name.substring(13)+" : coefficient" : name;
    };}
    private void lock(){state.lock().orElseThrow(()->new InvalidOperationException("Initialisation manquante"));}
    private <E> E require(JpaRepository<E,Long> repository,Long id){if(id==null) throw new InvalidOperationException("Identifiant obligatoire");return repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Enregistrement introuvable : "+id));}
    private void check(boolean valid,String message){if(!valid)throw new InvalidOperationException(message);}
    private void conflict(boolean valid,String message){if(!valid)throw new ConflictException(message);}
    private void audit(String resource,Long id,String action){var auth=SecurityContextHolder.getContext().getAuthentication();auditRepository.save(new AuditEvent(resource,id,action,auth==null ? "system" : auth.getName()));}
    @Override @Transactional(readOnly=true) public List<AuditResponse> history(String resource,Long id){return auditRepository.findByResourceAndResourceIdOrderByOccurredAtDesc(resource,id).stream().map(e->new AuditResponse(e.getAction(),e.getActor(),e.getOccurredAt())).toList();}
    @Override @Transactional(readOnly=true) public List<SettingResponse> settings(){return settingRepository.findAll().stream().sorted(Comparator.comparing(Setting::getName)).map(s->new SettingResponse(s.getName(),settingLabel(s.getName()),s.getValue())).toList();}
    @Override public void setting(SettingRequest r){lock();check(DEFAULTS.containsKey(r.name()) || r.name().startsWith("price.zone."),"Paramètre inconnu");check(!Set.of("commission","rate.subscriber").contains(r.name()) || r.value()<=1,"Ce taux doit être compris entre 0 et 1");check(!r.name().startsWith("points.") || r.value()==Math.floor(r.value()),"Les points doivent être entiers");settingRepository.save(new Setting(r.name(),r.value()));audit("Setting",0L,r.name()+" = "+r.value());}
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
    @Override @Transactional(readOnly=true) public ClientResponse getClientByEmail(String email) {return clientRepository.findByEmailIgnoreCase(email).map(ClientMapper::response).orElseThrow(()->new ResourceNotFoundException("Compte client introuvable"));}
    private void validateClient(Client e) {
        e.setEmail(e.getEmail().trim().toLowerCase(Locale.ROOT));
        if(clientRepository.findByEmailIgnoreCase(e.getEmail()).filter(v->!Objects.equals(v.getId(),e.getId())).isPresent()) throw new ConflictException("Cette adresse e-mail est déjà utilisée");
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
        if(deliveryOrderRepository.findAll().stream().anyMatch(v->Objects.equals(v.getClientId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Commandes");
        clientRepository.delete(e); audit("Client",id,"Suppression");
    }
    @Override public void setClientPassword(Long id,String rawPassword) {
        lock(); check(rawPassword!=null && rawPassword.length()>=8,"Le mot de passe doit contenir au moins 8 caractères");
        var client=require(clientRepository,id);client.setPasswordHash(passwordEncoder.encode(rawPassword));clientRepository.saveAndFlush(client);audit("Client",id,"Mot de passe défini");
    }

    @Override public DeliveryOrderResponse createClientOrder(Long clientId,String pickupAddress,String deliveryAddress,BigDecimal weight,BigDecimal volume,String priority,BigDecimal distance,String zone,LocalDateTime expectedAt,String description) {
        lock(); require(clientRepository,clientId);
        String code="LIV-"+LocalDate.now().toString().replace("-","")+"-"+UUID.randomUUID().toString().substring(0,6).toUpperCase(Locale.ROOT);
        var order=createDeliveryOrder(new DeliveryOrderRequest(code,clientId,pickupAddress,deliveryAddress,weight,volume,priority,distance,zone,expectedAt));
        createParcel(new ParcelRequest(order.id(),"COL-"+code,description,weight,volume));
        audit("DeliveryOrder",order.id(),"Commande créée par le client");
        return getDeliveryOrder(order.id());
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<DelivererResponse> listDeliverer(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","name","phone","email","vehicle");
        return PageResponse.of(delivererRepository.findAll(spec(q,"",filter,List.of("name","phone","email","vehicle"),allowed),safePage(pageable,allowed)).map(DelivererMapper::response));
    }
    @Override @Transactional(readOnly=true) public DelivererResponse getDeliverer(Long id) {return DelivererMapper.response(require(delivererRepository,id));}
    @Override @Transactional(readOnly=true) public DelivererResponse getDelivererByEmail(String email){return delivererRepository.findByEmailIgnoreCase(email).map(DelivererMapper::response).orElseThrow(()->new ResourceNotFoundException("Compte livreur introuvable"));}
    private void validateDeliverer(Deliverer e) {
        e.setEmail(e.getEmail().trim().toLowerCase(Locale.ROOT));
        if(delivererRepository.findByEmailIgnoreCase(e.getEmail()).filter(v->!Objects.equals(v.getId(),e.getId())).isPresent())throw new ConflictException("Cette adresse e-mail est déjà utilisée");
        validate(e);
    }
    @Override public DelivererResponse createDeliverer(DelivererRequest request) {
        lock(); var e=new Deliverer(); DelivererMapper.apply(request,e); validateDeliverer(e);
        delivererRepository.saveAndFlush(e); afterCreate(e); audit("Deliverer",e.getId(),"Création"); return DelivererMapper.response(e);
    }
    @Override public DelivererResponse updateDeliverer(Long id,DelivererRequest request) {
        lock(); var e=require(delivererRepository,id); beforeUpdate(e); DelivererMapper.apply(request,e); validateDeliverer(e);
        delivererRepository.saveAndFlush(e); audit("Deliverer",id,"Modification"); return DelivererMapper.response(e);
    }
    @Override public void deleteDeliverer(Long id) {
        lock(); var e=require(delivererRepository,id); beforeDelete(e);
        delivererRepository.delete(e); audit("Deliverer",id,"Suppression");
    }
    @Override public void setDelivererPassword(Long id,String rawPassword){lock();check(rawPassword!=null&&rawPassword.length()>=8,"Le mot de passe doit contenir au moins 8 caractères");var d=require(delivererRepository,id);d.setPasswordHash(passwordEncoder.encode(rawPassword));delivererRepository.saveAndFlush(d);audit("Deliverer",id,"Mot de passe défini");}

    @Override public DeliveryOrderResponse assignOrder(Long orderId,Long delivererId){
        lock();var o=require(deliveryOrderRepository,orderId);var d=require(delivererRepository,delivererId);
        check("CREEE".equals(o.getStatus()),"Cette commande a déjà été acceptée");
        o.setDelivererId(d.getId());o.setStatus("ASSIGNEE");
        audit("DeliveryOrder",o.getId(),"Acceptée par "+d.getName());return DeliveryOrderMapper.response(o);
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<DeliveryOrderResponse> listDeliveryOrder(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","number","clientId","pickupAddress","deliveryAddress","weight","volume","priority","distance","zone","expectedAt","price","delivererId","status","createdAt","deliveredAt");
        return PageResponse.of(deliveryOrderRepository.findAll(spec(q,status,filter,List.of("number","pickupAddress","deliveryAddress","priority","zone","status"),allowed),safePage(pageable,allowed)).map(DeliveryOrderMapper::response));
    }
    @Override @Transactional(readOnly=true) public DeliveryOrderResponse getDeliveryOrder(Long id) {return DeliveryOrderMapper.response(require(deliveryOrderRepository,id));}
    private void validateDeliveryOrder(DeliveryOrder e) {
        if(e.getClientId()!=null) require(clientRepository,e.getClientId());
        if(e.getPriority()==null || e.getPriority().isBlank()) e.setPriority("NORMALE");
        if(deliveryOrderRepository.findAll().stream().anyMatch(v->!Objects.equals(v.getId(),e.getId()) && Objects.equals(v.getNumber(),e.getNumber()))) throw new ConflictException("Numéro déjà utilisé");
        validate(e);
    }
    @Override public DeliveryOrderResponse createDeliveryOrder(DeliveryOrderRequest request) {
        lock(); var e=new DeliveryOrder(); DeliveryOrderMapper.apply(request,e); validateDeliveryOrder(e);
        deliveryOrderRepository.saveAndFlush(e); afterCreate(e); audit("DeliveryOrder",e.getId(),"Création"); return DeliveryOrderMapper.response(e);
    }
    @Override public DeliveryOrderResponse updateDeliveryOrder(Long id,DeliveryOrderRequest request) {
        lock(); var e=require(deliveryOrderRepository,id); beforeUpdate(e); DeliveryOrderMapper.apply(request,e); validateDeliveryOrder(e);
        deliveryOrderRepository.saveAndFlush(e); audit("DeliveryOrder",id,"Modification"); return DeliveryOrderMapper.response(e);
    }
    @Override public void deleteDeliveryOrder(Long id) {
        lock(); var e=require(deliveryOrderRepository,id); beforeDelete(e);
        if(parcelRepository.findAll().stream().anyMatch(v->Objects.equals(v.getOrderId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Colis");
        if(incidentRepository.findAll().stream().anyMatch(v->Objects.equals(v.getOrderId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Incidents");
        if(deliveryProofRepository.findAll().stream().anyMatch(v->Objects.equals(v.getOrderId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Preuves de livraison");
        deliveryOrderRepository.delete(e); audit("DeliveryOrder",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<ParcelResponse> listParcel(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","orderId","reference","description","weight","volume");
        return PageResponse.of(parcelRepository.findAll(spec(q,status,filter,List.of("reference","description"),allowed),safePage(pageable,allowed)).map(ParcelMapper::response));
    }
    @Override @Transactional(readOnly=true) public ParcelResponse getParcel(Long id) {return ParcelMapper.response(require(parcelRepository,id));}
    private void validateParcel(Parcel e) {
        if(e.getOrderId()!=null) require(deliveryOrderRepository,e.getOrderId());
        if(parcelRepository.findAll().stream().anyMatch(v->!Objects.equals(v.getId(),e.getId()) && Objects.equals(v.getReference(),e.getReference()))) throw new ConflictException("Référence déjà utilisé");
        validate(e);
    }
    @Override public ParcelResponse createParcel(ParcelRequest request) {
        lock(); var e=new Parcel(); ParcelMapper.apply(request,e); validateParcel(e);
        parcelRepository.saveAndFlush(e); afterCreate(e); audit("Parcel",e.getId(),"Création"); return ParcelMapper.response(e);
    }
    @Override public ParcelResponse updateParcel(Long id,ParcelRequest request) {
        lock(); var e=require(parcelRepository,id); beforeUpdate(e); ParcelMapper.apply(request,e); validateParcel(e);
        parcelRepository.saveAndFlush(e); audit("Parcel",id,"Modification"); return ParcelMapper.response(e);
    }
    @Override public void deleteParcel(Long id) {
        lock(); var e=require(parcelRepository,id); beforeDelete(e);
        parcelRepository.delete(e); audit("Parcel",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<IncidentResponse> listIncident(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","orderId","type","description","resolution","delivererId","occurredAt");
        return PageResponse.of(incidentRepository.findAll(spec(q,status,filter,List.of("type","description","resolution"),allowed),safePage(pageable,allowed)).map(IncidentMapper::response));
    }
    @Override @Transactional(readOnly=true) public IncidentResponse getIncident(Long id) {return IncidentMapper.response(require(incidentRepository,id));}
    private void validateIncident(Incident e) {
        if(e.getOrderId()!=null) require(deliveryOrderRepository,e.getOrderId());
        if(e.getType()==null || e.getType().isBlank()) e.setType("CLIENT_ABSENT");
        validate(e);
    }
    @Override public IncidentResponse createIncident(IncidentRequest request) {
        lock(); var e=new Incident(); IncidentMapper.apply(request,e); validateIncident(e);
        incidentRepository.saveAndFlush(e); afterCreate(e); audit("Incident",e.getId(),"Création"); return IncidentMapper.response(e);
    }
    @Override public IncidentResponse updateIncident(Long id,IncidentRequest request) {
        lock(); var e=require(incidentRepository,id); beforeUpdate(e); IncidentMapper.apply(request,e); validateIncident(e);
        incidentRepository.saveAndFlush(e); audit("Incident",id,"Modification"); return IncidentMapper.response(e);
    }
    @Override public void deleteIncident(Long id) {
        lock(); var e=require(incidentRepository,id); beforeDelete(e);
        incidentRepository.delete(e); audit("Incident",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<DeliveryProofResponse> listDeliveryProof(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","orderId","recipient","confirmationCode","comment","deliveredAt");
        return PageResponse.of(deliveryProofRepository.findAll(spec(q,status,filter,List.of("recipient","confirmationCode","comment"),allowed),safePage(pageable,allowed)).map(DeliveryProofMapper::response));
    }
    @Override @Transactional(readOnly=true) public DeliveryProofResponse getDeliveryProof(Long id) {return DeliveryProofMapper.response(require(deliveryProofRepository,id));}
    private void validateDeliveryProof(DeliveryProof e) {
        if(e.getOrderId()!=null) require(deliveryOrderRepository,e.getOrderId());
        validate(e);
    }
    @Override public DeliveryProofResponse createDeliveryProof(DeliveryProofRequest request) {
        lock(); var e=new DeliveryProof(); DeliveryProofMapper.apply(request,e); validateDeliveryProof(e);
        deliveryProofRepository.saveAndFlush(e); afterCreate(e); audit("DeliveryProof",e.getId(),"Création"); return DeliveryProofMapper.response(e);
    }
    @Override public DeliveryProofResponse updateDeliveryProof(Long id,DeliveryProofRequest request) {
        lock(); var e=require(deliveryProofRepository,id); beforeUpdate(e); DeliveryProofMapper.apply(request,e); validateDeliveryProof(e);
        deliveryProofRepository.saveAndFlush(e); audit("DeliveryProof",id,"Modification"); return DeliveryProofMapper.response(e);
    }
    @Override public void deleteDeliveryProof(Long id) {
        lock(); var e=require(deliveryProofRepository,id); beforeDelete(e);
        deliveryProofRepository.delete(e); audit("DeliveryProof",id,"Suppression");
    }

    public static final Map<String,Double> DEFAULTS=Map.ofEntries(Map.entry("price.base",5.0),Map.entry("price.km",1.5),Map.entry("price.kg",0.5),Map.entry("price.volume",0.2),Map.entry("price.urgent",1.5),Map.entry("price.veryUrgent",2.0),Map.entry("price.zone",1.0),Map.entry("score.distance",1.0),Map.entry("score.load",2.0),Map.entry("score.priority",1.0));
    private boolean active(DeliveryOrder o){return Set.of("ASSIGNEE","PRISE_EN_CHARGE","EN_TRANSIT").contains(o.getStatus());}
    private List<DeliveryOrder> load(Long id){return deliveryOrderRepository.findAll().stream().filter(o->active(o)&&Objects.equals(o.getDelivererId(),id)).toList();}
    private void draft(DeliveryOrder o){check("CREEE".equals(o.getStatus()),"La commande est déjà attribuée");}
    private void validate(Object obj){
        if(obj instanceof DeliveryOrder o){draft(o);var parcels=parcelRepository.findAll().stream().filter(v->Objects.equals(v.getOrderId(),o.getId())).toList();check(parcels.stream().mapToDouble(v->v.getWeight().doubleValue()).sum()<=o.getWeight().doubleValue(),"Poids déclaré inférieur aux colis");double priority="TRES_URGENTE".equals(o.getPriority())?cfg("price.veryUrgent"):"URGENTE".equals(o.getPriority())?cfg("price.urgent"):1;o.setPrice(money((cfg("price.base")+o.getWeight().doubleValue()*cfg("price.kg"))*priority));if(o.getCreatedAt()==null)o.setCreatedAt(LocalDateTime.now());}
        if(obj instanceof Parcel p){var o=require(deliveryOrderRepository,p.getOrderId());draft(o);var parcels=parcelRepository.findAll().stream().filter(v->v.getOrderId().equals(p.getOrderId())&&!Objects.equals(v.getId(),p.getId())).toList();check(parcels.stream().mapToDouble(v->v.getWeight().doubleValue()).sum()+p.getWeight().doubleValue()<=o.getWeight().doubleValue(),"Poids des colis supérieur au poids déclaré");check(parcels.stream().mapToDouble(v->v.getVolume().doubleValue()).sum()+p.getVolume().doubleValue()<=o.getVolume().doubleValue(),"Volume des colis supérieur au volume déclaré");}
        if(obj instanceof Deliverer d)check(load(d.getId()).isEmpty(),"Le livreur a des livraisons actives");
        if(obj instanceof Incident i){var o=require(deliveryOrderRepository,i.getOrderId());check(active(o),"Incident possible uniquement sur une livraison active");i.setDelivererId(o.getDelivererId());i.setOccurredAt(LocalDateTime.now());}
        if(obj instanceof DeliveryProof proof){var o=require(deliveryOrderRepository,proof.getOrderId());check("EN_TRANSIT".equals(o.getStatus()),"La commande n'est pas en transit");check(!proof.getDeliveredAt().isAfter(LocalDateTime.now())&&!proof.getDeliveredAt().isBefore(o.getCreatedAt()),"Date de preuve invalide");conflict(deliveryProofRepository.findAll().stream().noneMatch(v->v.getOrderId().equals(proof.getOrderId())&&!Objects.equals(v.getId(),proof.getId())),"Preuve déjà enregistrée");}
    }
    private void beforeUpdate(Object obj){if(obj instanceof DeliveryOrder o)draft(o);if(obj instanceof Parcel p)draft(require(deliveryOrderRepository,p.getOrderId()));if(obj instanceof Incident)throw new InvalidOperationException("Utilisez la résolution d'incident");if(obj instanceof DeliveryProof)throw new InvalidOperationException("Une preuve ne peut plus être modifiée");}
    private void beforeDelete(Object obj){beforeUpdate(obj);if(obj instanceof Deliverer d)conflict(deliveryOrderRepository.findAll().stream().noneMatch(o->Objects.equals(o.getDelivererId(),d.getId())),"Livreur utilisé dans l'historique");}
    private void afterCreate(Object obj){if(obj instanceof DeliveryProof proof)finish(require(deliveryOrderRepository,proof.getOrderId()),proof);if(obj instanceof Incident i){var o=require(deliveryOrderRepository,i.getOrderId());o.setStatus("ECHEC_LIVRAISON");release(o.getDelivererId());audit("DeliveryOrder",o.getId(),"Échec : "+i.getType());}}
    private void release(Long id){}
    private double score(Deliverer d,DeliveryOrder o){int priority="TRES_URGENTE".equals(o.getPriority())?3:"URGENTE".equals(o.getPriority())?2:1;return d.getDistanceToPickup().doubleValue()*cfg("score.distance")*priority*cfg("score.priority")+load(d.getId()).size()*cfg("score.load");}
    private void assign(DeliveryOrder o){draft(o);var d=delivererRepository.findAll().stream().min(Comparator.comparingInt((Deliverer v)->load(v.getId()).size()).thenComparing(Deliverer::getId)).orElseThrow(()->new ConflictException("Aucun livreur enregistré"));o.setDelivererId(d.getId());o.setStatus("ASSIGNEE");audit("DeliveryOrder",o.getId(),"Attribution : "+d.getName());}
    private void finish(DeliveryOrder o,DeliveryProof proof){o.setStatus("LIVREE");o.setDeliveredAt(proof.getDeliveredAt());release(o.getDelivererId());audit("DeliveryOrder",o.getId(),"Livraison confirmée : "+proof.getRecipient());}
    @Override public Object perform(String resource,Long id,String operation,ActionRequest r){lock();
        if("Incident".equals(resource)){var i=require(incidentRepository,id);check(r.comment()!=null&&!r.comment().isBlank(),"Résolution obligatoire");i.setResolution(r.comment());audit("Incident",id,"Résolution : "+r.comment());return IncidentMapper.response(i);}
        var o=require(deliveryOrderRepository,id);
        switch(operation){case "assign"->assign(o);case "pickup"->{check("ASSIGNEE".equals(o.getStatus()),"Attribuez la commande avant l'enlèvement");o.setStatus("PRISE_EN_CHARGE");audit(resource,id,"Prise en charge");}case "start"->{check("PRISE_EN_CHARGE".equals(o.getStatus()),"Effectuez l'enlèvement avant le départ");o.setStatus("EN_TRANSIT");audit(resource,id,"Départ");}
        case "deliver"->{check(r.recipient()!=null&&!r.recipient().isBlank()&&r.confirmationCode()!=null&&!r.confirmationCode().isBlank(),"Réceptionnaire et code de confirmation obligatoires");check(r.at()!=null,"Date et heure de livraison obligatoires");return createDeliveryProof(new DeliveryProofRequest(id,r.recipient(),r.confirmationCode(),r.comment(),r.at()));}
        case "cancel"->{check(Set.of("CREEE","ASSIGNEE").contains(o.getStatus()),"Annulation impossible après enlèvement");o.setStatus("ANNULEE");release(o.getDelivererId());audit(resource,id,"Annulation");}
        case "fail"->{check(active(o),"Cette livraison n'est pas active");check(r.comment()!=null&&!r.comment().isBlank(),"Le motif de l'échec est obligatoire");o.setStatus("ECHEC_LIVRAISON");release(o.getDelivererId());audit(resource,id,"Échec : "+r.comment());}
        case "return"->{check("ECHEC_LIVRAISON".equals(o.getStatus()),"Retour réservé aux livraisons en échec");o.setStatus("RETOURNEE");audit(resource,id,"Retour du colis");}
        default->throw new InvalidOperationException("Action inconnue");}return DeliveryOrderMapper.response(o);
    }
    @Override @Transactional(readOnly=true) public AnalyticsResponse statistics(LocalDate from,LocalDate to,Long actorId,String zone){dates(from,to);var all=deliveryOrderRepository.findAll().stream().filter(o->between(o.getCreatedAt().toLocalDate(),from,to)&&(actorId==null||actorId.equals(o.getDelivererId()))&&(zone==null||zone.isBlank()||zone.equalsIgnoreCase(o.getZone()))).toList();var done=all.stream().filter(o->"LIVREE".equals(o.getStatus())).toList();double revenue=done.stream().mapToDouble(o->o.getPrice().doubleValue()).sum();var metrics=new LinkedHashMap<String,Number>();metrics.put("Commandes",all.size());metrics.put("Livraisons",done.size());metrics.put("Réussite (%)",all.isEmpty()?0:100.0*done.size()/all.size());metrics.put("Échec (%)",all.isEmpty()?0:100.0*all.stream().filter(o->Set.of("ECHEC_LIVRAISON","RETOURNEE").contains(o.getStatus())).count()/all.size());metrics.put("Délai moyen (h)",done.stream().mapToLong(o->Duration.between(o.getCreatedAt(),o.getDeliveredAt()).toMinutes()).average().orElse(0)/60);metrics.put("Chiffre d'affaires",money(revenue));metrics.put("Revenu moyen",done.isEmpty()?0:money(revenue/done.size()));metrics.put("Distance (km)",done.stream().mapToDouble(o->o.getDistance().doubleValue()).sum());
        var groups=new LinkedHashMap<String,Map<String,Number>>();for(var d:delivererRepository.findAll()){var list=done.stream().filter(o->Objects.equals(o.getDelivererId(),d.getId())).toList();groups.put("Livreur "+d.getId()+" · "+d.getName(),Map.of("Livraisons",list.size(),"Revenu",money(list.stream().mapToDouble(o->o.getPrice().doubleValue()).sum())));}for(var day:done.stream().map(o->o.getDeliveredAt().toLocalDate().toString()).distinct().toList())groups.put("Jour "+day,Map.of("Livraisons",done.stream().filter(o->o.getDeliveredAt().toLocalDate().toString().equals(day)).count()));return new AnalyticsResponse(metrics,groups);
    }
    @Override @Transactional(readOnly=true) public void detail(String resource,Long id,Model m){if("DeliveryOrder".equals(resource)){m.addAttribute("parcels",parcelRepository.findAll().stream().filter(v->v.getOrderId().equals(id)).map(ParcelMapper::response).toList());m.addAttribute("incidents",incidentRepository.findAll().stream().filter(v->v.getOrderId().equals(id)).map(IncidentMapper::response).toList());m.addAttribute("proofs",deliveryProofRepository.findAll().stream().filter(v->v.getOrderId().equals(id)).map(DeliveryProofMapper::response).toList());var o=require(deliveryOrderRepository,id);if(o.getDelivererId()!=null)m.addAttribute("deliverer",getDeliverer(o.getDelivererId()));}}

}
