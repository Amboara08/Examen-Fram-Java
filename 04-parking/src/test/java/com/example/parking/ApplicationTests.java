package com.example.parking;
import java.time.*;
import java.math.*;
import java.util.*;
import com.example.parking.service.ApplicationService;
import com.example.parking.dto.request.*;
import com.example.parking.dto.response.*;
import com.example.parking.exception.*;
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
    @org.junit.jupiter.api.BeforeEach void cleanDatabase(){jdbc.update("delete from parkingstay_records");jdbc.update("delete from reservation_records");jdbc.update("delete from subscription_records");jdbc.update("delete from parkingspace_records");jdbc.update("delete from vehicle_records");jdbc.update("delete from customer_records");jdbc.update("delete from audit_event");}
    @Autowired ApplicationService service;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private String unique(){return UUID.randomUUID().toString().substring(0,8);}
    private BigDecimal bd(String value){return new BigDecimal(value);}
    private ActionRequest actionStatus(String status){return new ActionRequest(status,null,null,null,null,null,null,null,null,null,null,null);}
    private ActionRequest scores(int home,int away){return new ActionRequest(null,null,null,home,away,null,null,null,null,null,null,null);}
    private ActionRequest quantity(int quantity){return new ActionRequest(null,null,quantity,null,null,"Responsable","",null,null,null,null,null);}
    @Test void httpSecurityAndValidation() throws Exception {
        mvc.perform(get("/api/reservations").with(anonymous())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/reservations").with(user("user").roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reservations/99999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/reservations").param("size","2").param("sort","unknown,asc")).andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/settings").with(user("operator").roles("OPERATOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/reservations").with(user("operator").roles("OPERATOR"))).andExpect(status().isForbidden());
    }
    @Test void pagesRender() throws Exception {
        for(String path:List.of("/","/statistics","/settings","/users","/users/new","/vehicles","/vehicles/new","/parking/spaces","/parking/spaces/new","/subscriptions","/subscriptions/new","/reservations","/reservations/new","/parking/stays","/parking/stays/new"))mvc.perform(get(path)).andExpect(status().isOk());
    }
    private CustomerResponse customer(String type){String id=unique();return service.createCustomer(new CustomerRequest("Usager "+id,"client-"+id+"@example.mg","034",type));}
    private VehicleResponse vehicle(Long id){return service.createVehicle(new VehicleRequest("PLATE-"+unique(),id,"VOITURE"));}
    private ParkingSpaceResponse space(String type){return service.createParkingSpace(new ParkingSpaceRequest("S-"+unique(),"A",type,"LIBRE"));}
    @Test void reservationConflictsPriorityAndEntryExitBilling() throws Exception {
        var c=customer("VIP");var v=vehicle(c.id());var vip=space("VIP");space("STANDARD");LocalDateTime now=LocalDateTime.now();var reservation=service.createReservation(new ReservationRequest(c.id(),v.id(),vip.id(),now.plusDays(1),now.plusDays(1).plusHours(3)));assertEquals(vip.id(),reservation.spaceId());var c2=customer("VIP");var v2=vehicle(c2.id());assertThrows(ConflictException.class,()->service.createReservation(new ReservationRequest(c2.id(),v2.id(),vip.id(),now.plusDays(1).plusHours(1),now.plusDays(1).plusHours(4))));
        var stay=service.createParkingStay(new ParkingStayRequest(c.id(),v.id(),vip.id(),null,now.minusHours(5),now.minusHours(2)));assertEquals("OCCUPEE",service.getParkingSpace(vip.id()).status());assertThrows(ConflictException.class,()->service.createParkingStay(new ParkingStayRequest(c.id(),v.id(),null,null,now.minusMinutes(1),now.plusHours(1))));service.perform("ParkingStay",stay.id(),"exit",new ActionRequest(null,null,null,null,null,null,null,null,null,now,null,null));var finished=service.getParkingStay(stay.id());assertEquals(bd("10.80"),finished.amount());assertEquals("LIBRE",service.getParkingSpace(vip.id()).status());mvc.perform(get("/parking/stays/"+stay.id())).andExpect(status().isOk());
    }
    @Test void subscriptionValidityAndProtectedSpaces() throws Exception {var c=customer("STANDARD");var v=vehicle(c.id());var protectedSpace=space("PMR");LocalDateTime now=LocalDateTime.now();assertThrows(InvalidOperationException.class,()->service.createReservation(new ReservationRequest(c.id(),v.id(),protectedSpace.id(),now.plusDays(1),now.plusDays(1).plusHours(1))));var subscription=service.createSubscription(new SubscriptionRequest(c.id(),v.id(),"MENSUEL",now.toLocalDate().minusDays(1),now.toLocalDate().minusDays(1).plusMonths(1).minusDays(1),"ACTIF"));var reserved=space("ABONNE");var stay=service.createParkingStay(new ParkingStayRequest(c.id(),v.id(),reserved.id(),null,now.minusHours(1),now.plusHours(1)));service.perform("ParkingStay",stay.id(),"exit",new ActionRequest(null,null,null,null,null,null,null,null,null,now,null,null));assertEquals(bd("1.40"),service.getParkingStay(stay.id()).amount());mvc.perform(get("/subscriptions/"+subscription.id())).andExpect(status().isOk());}
    @Test void twoConcurrentReservationsCannotTakeSameSpace() throws Exception {
        var s=space("STANDARD");var c1=customer("STANDARD");var v1=vehicle(c1.id());var c2=customer("STANDARD");var v2=vehicle(c2.id());var now=LocalDateTime.now().plusDays(4);var requests=List.of(new ReservationRequest(c1.id(),v1.id(),s.id(),now,now.plusHours(1)),new ReservationRequest(c2.id(),v2.id(),s.id(),now,now.plusHours(1)));var pool=java.util.concurrent.Executors.newFixedThreadPool(2);try{var tasks=requests.stream().<java.util.concurrent.Callable<Boolean>>map(r->()->{try{service.createReservation(r);return true;}catch(ConflictException e){return false;}}).toList();var results=pool.invokeAll(tasks);int successes=0;for(var result:results)if(result.get())successes++;assertEquals(1,successes);}finally{pool.shutdownNow();}
    }

}

