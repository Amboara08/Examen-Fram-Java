package com.example.textile.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity public class AuditEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private String resource;
 private Long resourceId;
 private String action;
 private String actor;
 private LocalDateTime occurredAt;
 public AuditEvent() {}
 public AuditEvent(String resource,Long resourceId,String action,String actor) {this.resource=resource;this.resourceId=resourceId;this.action=action;this.actor=actor;this.occurredAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getResource(){return resource;} public Long getResourceId(){return resourceId;} public String getAction(){return action;} public String getActor(){return actor;} public LocalDateTime getOccurredAt(){return occurredAt;}
}
