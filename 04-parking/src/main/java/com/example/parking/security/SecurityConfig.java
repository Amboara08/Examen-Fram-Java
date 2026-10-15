package com.example.parking.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.http.HttpMethod;
import com.example.parking.repository.CustomerRepository;
import java.util.Map;
import java.util.Locale;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService users(PasswordEncoder encoder,CustomerRepository customers){
  var staff=Map.of(
   "admin",User.withUsername("admin").password(encoder.encode("Admin123!")).roles("ADMIN").build(),
   "manager",User.withUsername("manager").password(encoder.encode("Manager123!")).roles("MANAGER").build(),
   "operator",User.withUsername("operator").password(encoder.encode("Operator123!")).roles("OPERATOR").build(),
   "user",User.withUsername("user").password(encoder.encode("User123!")).roles("USER").build());
  return username->{
   String login=username==null?"":username.trim().toLowerCase(Locale.ROOT);
   var fixed=staff.get(login);if(fixed!=null)return fixed;return customers.findByEmailIgnoreCase(login)
   .filter(c->c.getPasswordHash()!=null&&!c.getPasswordHash().isBlank())
   .map(c->User.withUsername(c.getEmail()).password(c.getPasswordHash()).roles("CUSTOMER").build())
   .orElseThrow(()->new UsernameNotFoundException("Compte introuvable"));};
 }
 @Bean @org.springframework.core.annotation.Order(1)
 SecurityFilterChain api(HttpSecurity http) throws Exception {
  http.securityMatcher("/api/**").authorizeHttpRequests(auth->auth.requestMatchers("/api/settings/**").hasRole("ADMIN").anyRequest().authenticated());
  http.sessionManagement(s->s.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
  http.httpBasic(b->b.authenticationEntryPoint((request,response,e)->{response.setStatus(401);response.setContentType("application/json");response.getWriter().write("{\"status\":401,\"message\":\"Authentification requise\"}");}));
  http.csrf(csrf->csrf.disable());
  return http.build();
 }
 @Bean @org.springframework.core.annotation.Order(2)
 SecurityFilterChain browser(HttpSecurity http) throws Exception {
  http.authorizeHttpRequests(auth->auth
   .requestMatchers("/css/**","/login","/error").permitAll()
   .requestMatchers("/client/**").hasRole("CUSTOMER")
   .requestMatchers("/settings/**").hasRole("ADMIN")
   .anyRequest().hasAnyRole("ADMIN","MANAGER","OPERATOR","USER"));
  http.formLogin(form->form.loginPage("/login").successHandler((request,response,authentication)->{
   boolean customer=authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_CUSTOMER"));
   response.sendRedirect(customer?"/client":"/");
  }).permitAll()).logout(logout->logout
   .logoutUrl("/logout")
   .logoutSuccessUrl("/login?logout")
   .invalidateHttpSession(true)
   .clearAuthentication(true)
   .deleteCookies("JSESSIONID"));
  return http.build();
 }
}
