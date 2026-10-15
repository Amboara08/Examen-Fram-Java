package com.example.livraison;
import java.time.*;
import java.math.*;
import java.util.*;
import com.example.livraison.service.ApplicationService;
import com.example.livraison.dto.request.*;
import com.example.livraison.dto.response.*;
import com.example.livraison.exception.*;
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
    @org.junit.jupiter.api.BeforeEach void cleanDatabase(){jdbc.update("delete from deliveryproof_records");jdbc.update("delete from incident_records");jdbc.update("delete from parcel_records");jdbc.update("delete from deliveryorder_records");jdbc.update("delete from deliverer_records");jdbc.update("delete from client_records");jdbc.update("delete from audit_event");}
    @Autowired ApplicationService service;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private String unique(){return UUID.randomUUID().toString().substring(0,8);}
    private BigDecimal bd(String value){return new BigDecimal(value);}
    private ActionRequest actionStatus(String status){return new ActionRequest(status,null,null,null,null,null,null,null,null,null,null,null);}
    private ActionRequest scores(int home,int away){return new ActionRequest(null,null,null,home,away,null,null,null,null,null,null,null);}
    private ActionRequest quantity(int quantity){return new ActionRequest(null,null,quantity,null,null,"Responsable","",null,null,null,null,null);}
    @Test void httpSecurityAndValidation() throws Exception {
        mvc.perform(get("/api/orders").with(anonymous())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/orders").with(user("user").roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/orders/99999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/orders").param("size","2").param("sort","unknown,asc")).andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/settings").with(user("operator").roles("OPERATOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/orders").with(user("operator").roles("OPERATOR"))).andExpect(status().isForbidden());
    }
    @Test void pagesRender() throws Exception {
        for(String path:List.of("/","/statistics","/settings","/clients","/clients/new","/deliverers","/deliverers/new","/orders","/orders/new","/parcels","/parcels/new","/incidents","/incidents/new","/proofs","/proofs/new"))mvc.perform(get(path)).andExpect(status().isOk());
    }
    private DeliveryOrderResponse order(){var c=service.createClient(new ClientRequest("Client "+unique(),"c@example.mg","034","Ville"));return service.createDeliveryOrder(new DeliveryOrderRequest("ORD-"+unique(),c.id(),"A","B",bd("5"),bd("10"),"URGENTE",bd("10"),uniqueZone,LocalDateTime.now().plusDays(1)));}
    private final String uniqueZone=UUID.randomUUID().toString();
    private DelivererResponse deliverer(BigDecimal weight){return service.createDeliverer(new DelivererRequest("Livreur "+unique(),"034","Moto",uniqueZone,weight,bd("100"),bd("1"),2,"DISPONIBLE"));}
    @Test void capacityWorkflowProofAndNoBackwardTransition() throws Exception {
        deliverer(bd("1"));var d=deliverer(bd("10"));var o=order();service.createParcel(new ParcelRequest(o.id(),"P-"+unique(),"Colis",bd("5"),bd("10")));service.perform("DeliveryOrder",o.id(),"assign",ActionRequest.empty());assertEquals(d.id(),service.getDeliveryOrder(o.id()).delivererId());assertThrows(InvalidOperationException.class,()->service.perform("DeliveryOrder",o.id(),"start",ActionRequest.empty()));service.perform("DeliveryOrder",o.id(),"pickup",ActionRequest.empty());service.perform("DeliveryOrder",o.id(),"start",ActionRequest.empty());assertThrows(InvalidOperationException.class,()->service.perform("DeliveryOrder",o.id(),"deliver",ActionRequest.empty()));var at=LocalDateTime.now();service.perform("DeliveryOrder",o.id(),"deliver",new ActionRequest(null,null,null,null,null,null,"Reçu","Rabe","ABC123",at,null,null));assertEquals("LIVREE",service.getDeliveryOrder(o.id()).status());assertEquals("DISPONIBLE",service.getDeliverer(d.id()).status());assertThrows(InvalidOperationException.class,()->service.perform("DeliveryOrder",o.id(),"start",ActionRequest.empty()));assertEquals(1,service.listDeliveryProof("","","orderId:"+o.id(),PageRequest.of(0,10)).totalElements());mvc.perform(get("/orders/"+o.id())).andExpect(status().isOk());mvc.perform(get("/api/orders/"+o.id()+"/tracking")).andExpect(status().isOk());
    }
    @Test void incidentFailureResolutionAndReturn() throws Exception {deliverer(bd("20"));var o=order();service.perform("DeliveryOrder",o.id(),"assign",ActionRequest.empty());var i=service.createIncident(new IncidentRequest(o.id(),"CLIENT_ABSENT","Pas de réponse",""));assertEquals("ECHEC_LIVRAISON",service.getDeliveryOrder(o.id()).status());assertNotNull(i.delivererId());service.perform("Incident",i.id(),"resolve",new ActionRequest(null,null,null,null,null,null,"Retour à l'expéditeur",null,null,null,null,null));service.perform("DeliveryOrder",o.id(),"return",ActionRequest.empty());assertEquals("RETOURNEE",service.getDeliveryOrder(o.id()).status());mvc.perform(get("/incidents/"+i.id())).andExpect(status().isOk());}

}
