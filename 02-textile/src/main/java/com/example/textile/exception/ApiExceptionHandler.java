package com.example.textile.exception;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.*;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
import java.time.*;
@RestControllerAdvice(basePackages="com.example.textile.controller.rest")
public class ApiExceptionHandler {
 public record ErrorResponse(int status,String message,Map<String,String> errors,LocalDateTime timestamp) {}
 private ResponseEntity<ErrorResponse> error(int status,String message,Map<String,String> fields) {return ResponseEntity.status(status).body(new ErrorResponse(status,message,fields,LocalDateTime.now()));}
 @ExceptionHandler(BusinessException.class) public ResponseEntity<ErrorResponse> business(BusinessException e) {return error(e instanceof ResourceNotFoundException ? 404 : e instanceof ConflictException ? 409 : 422,e.getMessage(),Map.of());}
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {Map<String,String> fields=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(f->fields.putIfAbsent(f.getField(),f.getDefaultMessage()));return error(400,"Données invalides",fields);}
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,IllegalArgumentException.class}) public ResponseEntity<ErrorResponse> invalid(Exception e) {return error(400,"Valeur ou format invalide",Map.of());}
 @ExceptionHandler({DataIntegrityViolationException.class,ConcurrencyFailureException.class}) public ResponseEntity<ErrorResponse> conflict(Exception e) {return error(409,"Conflit : cet enregistrement existe déjà ou a été modifié",Map.of());}
 @ExceptionHandler(AccessDeniedException.class) public ResponseEntity<ErrorResponse> denied(Exception e) {return error(403,"Accès refusé",Map.of());}
}
