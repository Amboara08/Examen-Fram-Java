package com.example.textile.service.impl;

import java.time.*;
import java.math.*;
import java.util.*;
import com.example.textile.service.ApplicationService;
import com.example.textile.entity.*;
import com.example.textile.repository.*;
import com.example.textile.mapper.*;
import com.example.textile.dto.request.*;
import com.example.textile.dto.response.*;
import com.example.textile.exception.*;
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
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final TextileOrderRepository textileOrderRepository;
    private final OrderLineRepository orderLineRepository;
    private final ProductionStepRepository productionStepRepository;
    private final QualityControlRepository qualityControlRepository;

    private final SystemStateRepository state;
    private final AuditEventRepository auditRepository;
    private final SettingRepository settingRepository;
    public ApplicationServiceImpl(ClientRepository clientRepository, ProductRepository productRepository, TextileOrderRepository textileOrderRepository, OrderLineRepository orderLineRepository, ProductionStepRepository productionStepRepository, QualityControlRepository qualityControlRepository,SystemStateRepository state,AuditEventRepository auditRepository,SettingRepository settingRepository) {
        this.clientRepository=clientRepository;
        this.productRepository=productRepository;
        this.textileOrderRepository=textileOrderRepository;
        this.orderLineRepository=orderLineRepository;
        this.productionStepRepository=productionStepRepository;
        this.qualityControlRepository=qualityControlRepository;

        this.state=state; this.auditRepository=auditRepository; this.settingRepository=settingRepository;
    }
    private String settingLabel(String name){return switch(name){
        case "duration.coupe" -> "Coupe : durée prévue (jours)";
        case "duration.couture" -> "Couture : durée prévue (jours)";
        case "duration.finition" -> "Finition : durée prévue (jours)";
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
    private void validateClient(Client e) {
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
        if(textileOrderRepository.findAll().stream().anyMatch(v->Objects.equals(v.getClientId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Commandes");
        clientRepository.delete(e); audit("Client",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<ProductResponse> listProduct(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","name","reference","description","unitPrice");
        return PageResponse.of(productRepository.findAll(spec(q,status,filter,List.of("name","reference","description"),allowed),safePage(pageable,allowed)).map(ProductMapper::response));
    }
    @Override @Transactional(readOnly=true) public ProductResponse getProduct(Long id) {return ProductMapper.response(require(productRepository,id));}
    private void validateProduct(Product e) {
        if(productRepository.findAll().stream().anyMatch(v->!Objects.equals(v.getId(),e.getId()) && Objects.equals(v.getReference(),e.getReference()))) throw new ConflictException("Référence déjà utilisé");
        validate(e);
    }
    @Override public ProductResponse createProduct(ProductRequest request) {
        lock(); var e=new Product(); ProductMapper.apply(request,e); validateProduct(e);
        productRepository.saveAndFlush(e); afterCreate(e); audit("Product",e.getId(),"Création"); return ProductMapper.response(e);
    }
    @Override public ProductResponse updateProduct(Long id,ProductRequest request) {
        lock(); var e=require(productRepository,id); beforeUpdate(e); ProductMapper.apply(request,e); validateProduct(e);
        productRepository.saveAndFlush(e); audit("Product",id,"Modification"); return ProductMapper.response(e);
    }
    @Override public void deleteProduct(Long id) {
        lock(); var e=require(productRepository,id); beforeDelete(e);
        if(orderLineRepository.findAll().stream().anyMatch(v->Objects.equals(v.getProductId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Lignes de commande");
        productRepository.delete(e); audit("Product",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<TextileOrderResponse> listTextileOrder(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","number","clientId","orderDate","deliveryDate","priority","status","deliveredAt");
        Specification<TextileOrder> selection=spec(q,status,filter,List.of("number","priority","status"),allowed);
        if(pageable.getSort().isUnsorted()){var original=selection;selection=(root,query,cb)->{var predicate=original.toPredicate(root,query,cb);query.orderBy(cb.desc(cb.selectCase().when(cb.equal(root.get("priority"),"TRES_URGENTE"),3).when(cb.equal(root.get("priority"),"URGENTE"),2).otherwise(1)),cb.asc(root.get("deliveryDate")),cb.desc(root.get("id")));return predicate;};return PageResponse.of(textileOrderRepository.findAll(selection,PageRequest.of(pageable.getPageNumber(),Math.min(100,pageable.getPageSize()))).map(TextileOrderMapper::response));}
        return PageResponse.of(textileOrderRepository.findAll(selection,safePage(pageable,allowed)).map(TextileOrderMapper::response));
    }
    @Override @Transactional(readOnly=true) public TextileOrderResponse getTextileOrder(Long id) {return TextileOrderMapper.response(require(textileOrderRepository,id));}
    private void validateTextileOrder(TextileOrder e) {
        if(e.getClientId()!=null) require(clientRepository,e.getClientId());
        if(e.getPriority()==null || e.getPriority().isBlank()) e.setPriority("NORMALE");
        if(textileOrderRepository.findAll().stream().anyMatch(v->!Objects.equals(v.getId(),e.getId()) && Objects.equals(v.getNumber(),e.getNumber()))) throw new ConflictException("Numéro déjà utilisé");
        validate(e);
    }
    @Override public TextileOrderResponse createTextileOrder(TextileOrderRequest request) {
        lock(); var e=new TextileOrder(); TextileOrderMapper.apply(request,e); validateTextileOrder(e);
        textileOrderRepository.saveAndFlush(e); afterCreate(e); audit("TextileOrder",e.getId(),"Création"); return TextileOrderMapper.response(e);
    }
    @Override public TextileOrderResponse updateTextileOrder(Long id,TextileOrderRequest request) {
        lock(); var e=require(textileOrderRepository,id); beforeUpdate(e); TextileOrderMapper.apply(request,e); validateTextileOrder(e);
        textileOrderRepository.saveAndFlush(e); audit("TextileOrder",id,"Modification"); return TextileOrderMapper.response(e);
    }
    @Override public void deleteTextileOrder(Long id) {
        lock(); var e=require(textileOrderRepository,id); beforeDelete(e);
        if(orderLineRepository.findAll().stream().anyMatch(v->Objects.equals(v.getOrderId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Lignes de commande");
        if(productionStepRepository.findAll().stream().anyMatch(v->Objects.equals(v.getOrderId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Étapes");
        if(qualityControlRepository.findAll().stream().anyMatch(v->Objects.equals(v.getOrderId(),id))) throw new ConflictException("Cet enregistrement est utilisé : Contrôles qualité");
        textileOrderRepository.delete(e); audit("TextileOrder",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<OrderLineResponse> listOrderLine(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","orderId","productId","quantity","size","color","unitPrice","producedQuantity");
        return PageResponse.of(orderLineRepository.findAll(spec(q,status,filter,List.of("size","color"),allowed),safePage(pageable,allowed)).map(OrderLineMapper::response));
    }
    @Override @Transactional(readOnly=true) public OrderLineResponse getOrderLine(Long id) {return OrderLineMapper.response(require(orderLineRepository,id));}
    private void validateOrderLine(OrderLine e) {
        if(e.getOrderId()!=null) require(textileOrderRepository,e.getOrderId());
        if(e.getProductId()!=null) require(productRepository,e.getProductId());
        validate(e);
    }
    @Override public OrderLineResponse createOrderLine(OrderLineRequest request) {
        lock(); var e=new OrderLine(); OrderLineMapper.apply(request,e); validateOrderLine(e);
        orderLineRepository.saveAndFlush(e); afterCreate(e); audit("OrderLine",e.getId(),"Création"); return OrderLineMapper.response(e);
    }
    @Override public OrderLineResponse updateOrderLine(Long id,OrderLineRequest request) {
        lock(); var e=require(orderLineRepository,id); beforeUpdate(e); OrderLineMapper.apply(request,e); validateOrderLine(e);
        orderLineRepository.saveAndFlush(e); audit("OrderLine",id,"Modification"); return OrderLineMapper.response(e);
    }
    @Override public void deleteOrderLine(Long id) {
        lock(); var e=require(orderLineRepository,id); beforeDelete(e);
        orderLineRepository.delete(e); audit("OrderLine",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<ProductionStepResponse> listProductionStep(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","orderId","type","responsible","plannedStart","plannedEnd","comment","status","startedAt","completedAt","quantity");
        return PageResponse.of(productionStepRepository.findAll(spec(q,status,filter,List.of("type","responsible","comment","status"),allowed),safePage(pageable,allowed)).map(ProductionStepMapper::response));
    }
    @Override @Transactional(readOnly=true) public ProductionStepResponse getProductionStep(Long id) {return ProductionStepMapper.response(require(productionStepRepository,id));}
    private void validateProductionStep(ProductionStep e) {
        if(e.getOrderId()!=null) require(textileOrderRepository,e.getOrderId());
        if(e.getType()==null || e.getType().isBlank()) e.setType("COUPE");
        validate(e);
    }
    @Override public ProductionStepResponse createProductionStep(ProductionStepRequest request) {
        lock(); var e=new ProductionStep(); ProductionStepMapper.apply(request,e); validateProductionStep(e);
        productionStepRepository.saveAndFlush(e); afterCreate(e); audit("ProductionStep",e.getId(),"Création"); return ProductionStepMapper.response(e);
    }
    @Override public ProductionStepResponse updateProductionStep(Long id,ProductionStepRequest request) {
        lock(); var e=require(productionStepRepository,id); beforeUpdate(e); ProductionStepMapper.apply(request,e); validateProductionStep(e);
        productionStepRepository.saveAndFlush(e); audit("ProductionStep",id,"Modification"); return ProductionStepMapper.response(e);
    }
    @Override public void deleteProductionStep(Long id) {
        lock(); var e=require(productionStepRepository,id); beforeDelete(e);
        productionStepRepository.delete(e); audit("ProductionStep",id,"Suppression");
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<QualityControlResponse> listQualityControl(String q,String status,String filter,Pageable pageable) {
        Set<String> allowed=Set.of("id","orderId","result","responsible","comment","checkedAt");
        return PageResponse.of(qualityControlRepository.findAll(spec(q,status,filter,List.of("result","responsible","comment"),allowed),safePage(pageable,allowed)).map(QualityControlMapper::response));
    }
    @Override @Transactional(readOnly=true) public QualityControlResponse getQualityControl(Long id) {return QualityControlMapper.response(require(qualityControlRepository,id));}
    private void validateQualityControl(QualityControl e) {
        if(e.getOrderId()!=null) require(textileOrderRepository,e.getOrderId());
        if(e.getResult()==null || e.getResult().isBlank()) e.setResult("CONFORME");
        validate(e);
    }
    @Override public QualityControlResponse createQualityControl(QualityControlRequest request) {
        lock(); var e=new QualityControl(); QualityControlMapper.apply(request,e); validateQualityControl(e);
        qualityControlRepository.saveAndFlush(e); afterCreate(e); audit("QualityControl",e.getId(),"Création"); return QualityControlMapper.response(e);
    }
    @Override public QualityControlResponse updateQualityControl(Long id,QualityControlRequest request) {
        lock(); var e=require(qualityControlRepository,id); beforeUpdate(e); QualityControlMapper.apply(request,e); validateQualityControl(e);
        qualityControlRepository.saveAndFlush(e); audit("QualityControl",id,"Modification"); return QualityControlMapper.response(e);
    }
    @Override public void deleteQualityControl(Long id) {
        lock(); var e=require(qualityControlRepository,id); beforeDelete(e);
        qualityControlRepository.delete(e); audit("QualityControl",id,"Suppression");
    }

    public static final Map<String,Double> DEFAULTS=Map.of("duration.coupe",1.0,"duration.couture",2.0,"duration.finition",1.0);
    private static final List<String> STEPS=List.of("COUPE","COUTURE","FINITION");
    private List<OrderLine> lines(Long id){return orderLineRepository.findAll().stream().filter(l->l.getOrderId().equals(id)).toList();}
    private List<ProductionStep> steps(Long id){return productionStepRepository.findAll().stream().filter(s->s.getOrderId().equals(id)).sorted(Comparator.comparingInt(s->STEPS.indexOf(s.getType()))).toList();}
    private void draft(TextileOrder o){check("COMMANDE".equals(o.getStatus()),"La commande est déjà en production");}
    private void validate(Object obj){
        if(obj instanceof TextileOrder o){draft(o);check(!o.getDeliveryDate().isBefore(o.getOrderDate()),"La livraison précède la commande");}
        if(obj instanceof OrderLine l)draft(require(textileOrderRepository,l.getOrderId()));
        if(obj instanceof ProductionStep s){draft(require(textileOrderRepository,s.getOrderId()));check(s.getPlannedEnd().isAfter(s.getPlannedStart()),"La fin doit suivre le début");conflict(steps(s.getOrderId()).stream().noneMatch(v->!Objects.equals(v.getId(),s.getId()) && v.getType().equals(s.getType())),"Étape déjà planifiée");}
        if(obj instanceof QualityControl q){var o=require(textileOrderRepository,q.getOrderId());check("CONTROLE_QUALITE".equals(o.getStatus()),"Terminez la finition avant le contrôle qualité");q.setCheckedAt(LocalDateTime.now());}
    }
    private void beforeUpdate(Object obj){
        if(obj instanceof TextileOrder o)draft(o);if(obj instanceof OrderLine l)draft(require(textileOrderRepository,l.getOrderId()));if(obj instanceof ProductionStep s)draft(require(textileOrderRepository,s.getOrderId()));
        if(obj instanceof QualityControl)throw new InvalidOperationException("Enregistrez un nouveau contrôle pour conserver l'historique");
    }
    private void beforeDelete(Object obj){beforeUpdate(obj);}
    private void afterCreate(Object obj){}
    private void start(TextileOrder o){draft(o);check(!lines(o.getId()).isEmpty(),"Ajoutez au moins une ligne de commande");
        var planned=steps(o.getId());LocalDateTime cursor=LocalDateTime.now();for(String type:STEPS){var existing=planned.stream().filter(s->s.getType().equals(type)).findFirst();if(existing.isEmpty()){var s=new ProductionStep();s.setOrderId(o.getId());s.setType(type);s.setResponsible("Atelier");s.setPlannedStart(cursor);cursor=cursor.plusDays((long)cfg("duration."+type.toLowerCase(Locale.ROOT)));s.setPlannedEnd(cursor);productionStepRepository.save(s);}else cursor=existing.get().getPlannedEnd();}
        var all=steps(o.getId());for(int i=1;i<all.size();i++)check(!all.get(i).getPlannedStart().isBefore(all.get(i-1).getPlannedEnd()),"Les étapes planifiées se chevauchent");
        var first=all.get(0);first.setStatus("EN_COURS");first.setStartedAt(LocalDateTime.now());o.setStatus("COUPE");audit("TextileOrder",o.getId(),"Production démarrée");
    }
    private void complete(TextileOrder o,String type,ActionRequest r){
        check(STEPS.contains(type),"Étape inconnue");check(type.equals(o.getStatus()),"Terminez d'abord l'étape précédente");var all=steps(o.getId());var step=all.stream().filter(s->s.getType().equals(type)).findFirst().orElseThrow(()->new ResourceNotFoundException("Étape non planifiée"));check("EN_COURS".equals(step.getStatus()),"Étape non démarrée");check(r.quantity()!=null && r.quantity()>0,"Quantité positive obligatoire");
        int index=STEPS.indexOf(type),max=index==0?lines(o.getId()).stream().mapToInt(OrderLine::getQuantity).sum():all.get(index-1).getQuantity();check(r.quantity()<=max,"Quantité supérieure à celle de l'étape précédente");
        LocalDateTime end=r.at()==null?LocalDateTime.now():r.at();check(!end.isBefore(step.getStartedAt()) && !end.isAfter(LocalDateTime.now()),"Date réelle incohérente");
        step.setQuantity(r.quantity());step.setStatus("TERMINEE");step.setCompletedAt(end);if(r.responsible()!=null && !r.responsible().isBlank())step.setResponsible(r.responsible());step.setComment(r.comment());
        if(index<2){var next=all.get(index+1);next.setStatus("EN_COURS");next.setStartedAt(end);o.setStatus(next.getType());}
        else{int remaining=r.quantity();for(var line:lines(o.getId())){int produced=Math.min(line.getQuantity(),remaining);line.setProducedQuantity(produced);remaining-=produced;}o.setStatus("CONTROLE_QUALITE");}
        audit("ProductionStep",step.getId(),type+" : "+r.quantity());audit("TextileOrder",o.getId(),type+" terminée : "+r.quantity());
    }
    private void quality(TextileOrder o,ActionRequest r){check(Set.of("CONFORME","NON_CONFORME").contains(r.status()==null?"":r.status()),"Choisissez le résultat qualité");check(r.responsible()!=null && !r.responsible().isBlank(),"Responsable obligatoire");createQualityControl(new QualityControlRequest(o.getId(),r.status(),r.responsible(),r.comment()));audit("TextileOrder",o.getId(),"Qualité : "+r.status());}
    @Override public Object perform(String resource,Long id,String operation,ActionRequest r){lock();var o=require(textileOrderRepository,id);
        switch(operation){case "production/start"->start(o);case "COUPE","COUTURE","FINITION"->complete(o,operation,r);case "quality-control"->quality(o,r);case "delivery"->{check("CONTROLE_QUALITE".equals(o.getStatus()),"Production non terminée");var quality=qualityControlRepository.findAll().stream().filter(q->q.getOrderId().equals(id)).max(Comparator.comparing(QualityControl::getCheckedAt).thenComparing(QualityControl::getId)).orElseThrow(()->new InvalidOperationException("Contrôle qualité obligatoire"));check("CONFORME".equals(quality.getResult()),"La commande est non conforme");o.setStatus("LIVREE");o.setDeliveredAt(LocalDateTime.now());audit("TextileOrder",id,"Livraison");}default->throw new InvalidOperationException("Action inconnue");}return TextileOrderMapper.response(o);
    }
    @Override @Transactional(readOnly=true) public PageResponse<TextileOrderResponse> delayed(Pageable pageable){return PageResponse.of(textileOrderRepository.findAll((root,query,cb)->cb.and(cb.lessThan(root.get("deliveryDate"),LocalDate.now()),cb.notEqual(root.get("status"),"LIVREE")),safePage(pageable,Set.of("id","number","deliveryDate","priority","status"))).map(TextileOrderMapper::response));}
    private Map<String,Number> delays(TextileOrder o){var ss=steps(o.getId());long planned=ss.stream().mapToLong(s->Duration.between(s.getPlannedStart(),s.getPlannedEnd()).toHours()).sum();long real=ss.stream().filter(s->s.getStartedAt()!=null).mapToLong(s->Duration.between(s.getStartedAt(),s.getCompletedAt()==null?LocalDateTime.now():s.getCompletedAt()).toHours()).sum();LocalDate actual=o.getDeliveredAt()==null?LocalDate.now():o.getDeliveredAt().toLocalDate();long late=Math.max(0,ChronoUnit.DAYS.between(o.getDeliveryDate(),actual));long remaining=ss.stream().filter(s->!"TERMINEE".equals(s.getStatus())).mapToLong(s->(long)cfg("duration."+s.getType().toLowerCase(Locale.ROOT))).sum();if(ss.isEmpty())remaining=4;long forecast=ChronoUnit.DAYS.between(LocalDate.now(),o.getDeliveryDate());return Map.of("Durée prévue (h)",planned,"Durée réelle (h)",real,"Retard (jours)",late,"Risque de retard",!"LIVREE".equals(o.getStatus()) && remaining>forecast?1:0);}
    @Override @Transactional(readOnly=true) public AnalyticsResponse statistics(LocalDate from,LocalDate to,Long actorId,String zone){dates(from,to);var orders=textileOrderRepository.findAll().stream().filter(o->between(o.getOrderDate(),from,to)).toList();var metrics=new LinkedHashMap<String,Number>();metrics.put("Commandes",orders.size());metrics.put("En production",orders.stream().filter(o->!Set.of("COMMANDE","LIVREE").contains(o.getStatus())).count());metrics.put("En retard",orders.stream().filter(o->delays(o).get("Retard (jours)").longValue()>0).count());metrics.put("Livrées",orders.stream().filter(o->"LIVREE".equals(o.getStatus())).count());metrics.put("Pièces produites",orders.stream().flatMap(o->lines(o.getId()).stream()).mapToInt(OrderLine::getProducedQuantity).sum());var groups=new LinkedHashMap<String,Map<String,Number>>();for(var o:orders)groups.put(o.getNumber(),delays(o));return new AnalyticsResponse(metrics,groups);}
    @Override @Transactional(readOnly=true) public void detail(String resource,Long id,Model m){if("TextileOrder".equals(resource)){m.addAttribute("lines",lines(id).stream().map(OrderLineMapper::response).toList());m.addAttribute("steps",steps(id).stream().map(ProductionStepMapper::response).toList());m.addAttribute("quality",qualityControlRepository.findAll().stream().filter(q->q.getOrderId().equals(id)).map(QualityControlMapper::response).toList());var o=require(textileOrderRepository,id);m.addAttribute("delays",delays(o));long remaining=steps(id).stream().filter(s->!"TERMINEE".equals(s.getStatus())).mapToLong(s->(long)cfg("duration."+s.getType().toLowerCase(Locale.ROOT))).sum();m.addAttribute("forecast",o.getDeliveredAt()==null?LocalDate.now().plusDays(remaining):o.getDeliveredAt().toLocalDate());}}

}
