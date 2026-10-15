package com.example.textile.dto.response;

import java.util.Map;
public record AnalyticsResponse(Map<String,Number> metrics,Map<String,Map<String,Number>> groups) {}
