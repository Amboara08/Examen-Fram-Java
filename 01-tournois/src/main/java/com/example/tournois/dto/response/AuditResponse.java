package com.example.tournois.dto.response;

import java.time.LocalDateTime;
public record AuditResponse(String action,String actor,LocalDateTime occurredAt) {}
