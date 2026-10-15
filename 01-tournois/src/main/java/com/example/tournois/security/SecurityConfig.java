package com.example.tournois.security;

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
@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService users(PasswordEncoder encoder){return new InMemoryUserDetailsManager(
  User.withUsername("admin").password(encoder.encode("Admin123!")).roles("ADMIN").build(),
  User.withUsername("manager").password(encoder.encode("Manager123!")).roles("MANAGER").build(),
  User.withUsername("operator").password(encoder.encode("Operator123!")).roles("OPERATOR").build(),
  User.withUsername("user").password(encoder.encode("User123!")).roles("USER").build());}
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
  http.authorizeHttpRequests(auth->auth.requestMatchers("/css/**","/login","/error").permitAll().requestMatchers("/settings/**").hasRole("ADMIN").anyRequest().authenticated());
  http.formLogin(form->form.loginPage("/login").defaultSuccessUrl("/",true).permitAll()).logout(logout->logout.logoutSuccessUrl("/login?logout"));
  return http.build();
 }
}