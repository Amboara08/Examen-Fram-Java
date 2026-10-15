package com.example.textile.exception;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
@ControllerAdvice(basePackages="com.example.textile.controller.web")
public class WebExceptionHandler {
 @ExceptionHandler(BusinessException.class) public ModelAndView business(BusinessException e) {var m=new ModelAndView("error");m.setStatus(e instanceof ResourceNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.UNPROCESSABLE_ENTITY);m.addObject("message",e.getMessage());return m;}
 @ExceptionHandler(DataIntegrityViolationException.class) public ModelAndView conflict(Exception e) {var m=new ModelAndView("error");m.setStatus(HttpStatus.CONFLICT);m.addObject("message","Cet enregistrement existe déjà ou est utilisé.");return m;}
}
