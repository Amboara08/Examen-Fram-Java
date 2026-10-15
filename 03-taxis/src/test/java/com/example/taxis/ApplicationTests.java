package com.example.taxis;
import java.time.*;
import java.math.*;
import java.util.*;
import com.example.taxis.service.ApplicationService;
import com.example.taxis.dto.request.*;
import com.example.taxis.dto.response.*;
import com.example.taxis.exception.*;
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
    @org.junit.jupiter.api.BeforeEach void cleanDatabase(){jdbc.update("delete from ride_records");jdbc.update("delete from driver_records");jdbc.update("delete from vehicle_records");jdbc.update("delete from client_records");jdbc.update("delete from audit_event");}
    @Autowired ApplicationService service;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private String unique(){return UUID.randomUUID().toString().substring(0,8);}
    private BigDecimal bd(String value){return new BigDecimal(value);}
    private ActionRequest actionStatus(String status){return new ActionRequest(status,null,null,null,null,null,null,null,null,null,null,null);}
    private ActionRequest scores(int home,int away){return new ActionRequest(null,null,null,home,away,null,null,null,null,null,null,null);}
    private ActionRequest quantity(int quantity){return new ActionRequest(null,null,quantity,null,null,"Responsable","",null,null,null,null,null);}
    @Test void httpSecurityAndValidation() throws Exception {
        mvc.perform(get("/api/rides").with(anonymous())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/rides").with(user("user").roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/rides/99999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/rides").param("size","2").param("sort","unknown,asc")).andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/settings").with(user("operator").roles("OPERATOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/rides").with(user("operator").roles("OPERATOR"))).andExpect(status().isForbidden());
    }
    @Test void pagesRender() throws Exception {
        for(String path:List.of("/","/statistics","/settings","/clients","/clients/new","/vehicles","/vehicles/new","/drivers","/drivers/new","/rides","/rides/new"))mvc.perform(get(path)).andExpect(status().isOk());
    }
    private RideResponse ride(){var c=service.createClient(new ClientRequest("Client "+unique(),"client@example.mg","034","Ville"));return service.createRide(new RideRequest(c.id(),"A","B",bd("10"),LocalDateTime.now(),2));}
    private DriverResponse driver(String status,LocalDate expiry,String carStatus){var car=service.createVehicle(new VehicleRequest("CAR-"+unique(),"Toyota","Yaris",2022,4,bd("100"),carStatus));return service.createDriver(new DriverRequest("Chauffeur "+unique(),"034","PERMIS-"+unique(),expiry,LocalDate.now().minusYears(2),car.id(),bd("1"),status));}
    @Test void eligibilityDoubleAssignmentAndCompletion() throws Exception {
        driver("SUSPENDU",LocalDate.now().plusYears(1),"DISPONIBLE");driver("DISPONIBLE",LocalDate.now().minusDays(1),"DISPONIBLE");driver("DISPONIBLE",LocalDate.now().plusYears(1),"MAINTENANCE");var d=driver("DISPONIBLE",LocalDate.now().plusYears(1),"DISPONIBLE");var r=ride();service.perform("Ride",r.id(),"assign",ActionRequest.empty());assertEquals(d.id(),service.getRide(r.id()).driverId());assertThrows(InvalidOperationException.class,()->service.perform("Ride",r.id(),"assign",ActionRequest.empty()));var other=ride();assertThrows(ConflictException.class,()->service.perform("Ride",other.id(),"assign",ActionRequest.empty()));
        assertThrows(InvalidOperationException.class,()->service.perform("Ride",r.id(),"complete",ActionRequest.empty()));service.perform("Ride",r.id(),"start",ActionRequest.empty());service.perform("Ride",r.id(),"complete",ActionRequest.empty());assertEquals("DISPONIBLE",service.getDriver(d.id()).status());assertEquals(bd("110.00"),service.getVehicle(d.vehicleId()).mileage().setScale(2));assertEquals(bd("25.00"),service.getRide(r.id()).fare());mvc.perform(get("/rides/"+r.id())).andExpect(status().isOk());assertTrue(service.statistics(null,null,d.id(),"").metrics().get("Chiffre d'affaires").doubleValue()>0);
    }
    @Test void cancellationReleasesFleet() {var d=driver("DISPONIBLE",LocalDate.now().plusYears(1),"DISPONIBLE");var r=ride();service.perform("Ride",r.id(),"assign",ActionRequest.empty());Long assigned=service.getRide(r.id()).driverId();service.perform("Ride",r.id(),"cancel",ActionRequest.empty());assertEquals("DISPONIBLE",service.getDriver(assigned).status());assertThrows(InvalidOperationException.class,()->service.perform("Ride",r.id(),"start",ActionRequest.empty()));}

}
