package com.example.textile;
import java.time.*;
import java.math.*;
import java.util.*;
import com.example.textile.service.ApplicationService;
import com.example.textile.dto.request.*;
import com.example.textile.dto.response.*;
import com.example.textile.exception.*;
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
    @org.junit.jupiter.api.BeforeEach void cleanDatabase(){jdbc.update("delete from qualitycontrol_records");jdbc.update("delete from productionstep_records");jdbc.update("delete from orderline_records");jdbc.update("delete from textileorder_records");jdbc.update("delete from product_records");jdbc.update("delete from client_records");jdbc.update("delete from audit_event");}
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
        for(String path:List.of("/","/statistics","/settings","/clients","/clients/new","/products","/products/new","/orders","/orders/new","/order-lines","/order-lines/new","/production-steps","/production-steps/new","/quality-controls","/quality-controls/new"))mvc.perform(get(path)).andExpect(status().isOk());
    }
    private TextileOrderResponse order(){var c=service.createClient(new ClientRequest("Client "+unique(),"client@example.mg","034","Ville"));return service.createTextileOrder(new TextileOrderRequest("CMD-"+unique(),c.id(),LocalDate.now(),LocalDate.now().plusDays(7),"URGENTE"));}
    private void line(Long id){var p=service.createProduct(new ProductRequest("Chemise","REF-"+unique(),"",bd("10")));service.createOrderLine(new OrderLineRequest(id,p.id(),500,"M","Blanc",bd("10")));}
    @Test void strictWorkflowQuantitiesQualityAndDelivery() throws Exception {
        var o=order();assertThrows(InvalidOperationException.class,()->service.perform("TextileOrder",o.id(),"production/start",ActionRequest.empty()));line(o.id());service.perform("TextileOrder",o.id(),"production/start",ActionRequest.empty());
        assertThrows(InvalidOperationException.class,()->service.perform("TextileOrder",o.id(),"COUTURE",quantity(500)));assertThrows(InvalidOperationException.class,()->service.perform("TextileOrder",o.id(),"COUPE",quantity(501)));
        service.perform("TextileOrder",o.id(),"COUPE",quantity(500));service.perform("TextileOrder",o.id(),"COUTURE",quantity(480));assertThrows(InvalidOperationException.class,()->service.perform("TextileOrder",o.id(),"FINITION",quantity(500)));service.perform("TextileOrder",o.id(),"FINITION",quantity(450));
        assertThrows(InvalidOperationException.class,()->service.perform("TextileOrder",o.id(),"delivery",ActionRequest.empty()));service.createQualityControl(new QualityControlRequest(o.id(),"NON_CONFORME","QC","Défaut"));assertThrows(InvalidOperationException.class,()->service.perform("TextileOrder",o.id(),"delivery",ActionRequest.empty()));service.createQualityControl(new QualityControlRequest(o.id(),"CONFORME","QC","Vérifié"));service.perform("TextileOrder",o.id(),"delivery",ActionRequest.empty());assertEquals("LIVREE",service.getTextileOrder(o.id()).status());
        assertEquals(450,service.listOrderLine("","","orderId:"+o.id(),PageRequest.of(0,10)).content().get(0).producedQuantity());assertThrows(InvalidOperationException.class,()->service.updateTextileOrder(o.id(),new TextileOrderRequest(o.number(),o.clientId(),o.orderDate(),o.deliveryDate(),"NORMALE")));mvc.perform(get("/orders/"+o.id())).andExpect(status().isOk());
    }
    @Test void httpValidationAndWorkflowResponses() throws Exception {
        var o=order();line(o.id());mvc.perform(post("/api/orders/"+o.id()+"/production/start").with(csrf())).andExpect(status().isOk());mvc.perform(post("/api/orders/"+o.id()+"/production/steps/COUTURE/complete").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":500}")).andExpect(status().isUnprocessableEntity());mvc.perform(get("/api/orders/"+o.id()+"/production")).andExpect(status().isOk());
    }

}
