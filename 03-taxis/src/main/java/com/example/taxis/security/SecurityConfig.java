package com.example.taxis.security;

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
import com.example.taxis.repository.*;
import java.util.*;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService users(PasswordEncoder encoder,ClientRepository clients,DriverRepository drivers){var staff=Map.of(
  "admin",User.withUsername("admin").password(encoder.encode("Admin123!")).roles("ADMIN").build(),"manager",User.withUsername("manager").password(encoder.encode("Manager123!")).roles("MANAGER").build(),"operator",User.withUsername("operator").password(encoder.encode("Operator123!")).roles("OPERATOR").build(),"user",User.withUsername("user").password(encoder.encode("User123!")).roles("USER").build());
  return username->{String login=username==null?"":username.trim().toLowerCase(Locale.ROOT);var fixed=staff.get(login);if(fixed!=null)return fixed;var client=clients.findByEmailIgnoreCase(login).filter(c->c.getPasswordHash()!=null&&!c.getPasswordHash().isBlank()).map(c->User.withUsername(c.getEmail()).password(c.getPasswordHash()).roles("CUSTOMER").build());if(client.isPresent())return client.get();return drivers.findByEmailIgnoreCase(login).filter(d->d.getPasswordHash()!=null&&!d.getPasswordHash().isBlank()).map(d->User.withUsername(d.getEmail()).password(d.getPasswordHash()).roles("DRIVER").build()).orElseThrow(()->new UsernameNotFoundException("Compte introuvable"));};}
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
  http.authorizeHttpRequests(auth->auth.requestMatchers("/css/**","/login","/error").permitAll().requestMatchers("/client/**").hasRole("CUSTOMER").requestMatchers("/chauffeur/**").hasRole("DRIVER").requestMatchers("/settings/**").hasRole("ADMIN").anyRequest().hasAnyRole("ADMIN","MANAGER","OPERATOR","USER"));
  http.formLogin(form->form.loginPage("/login").successHandler((req,res,a)->{var roles=a.getAuthorities();boolean c=roles.stream().anyMatch(x->x.getAuthority().equals("ROLE_CUSTOMER"));boolean d=roles.stream().anyMatch(x->x.getAuthority().equals("ROLE_DRIVER"));res.sendRedirect(c?"/client":d?"/chauffeur":"/");}).permitAll()).logout(l->l.logoutSuccessUrl("/login?logout").invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID"));
  return http.build();
 }
}
