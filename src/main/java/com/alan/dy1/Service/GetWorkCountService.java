package com.alan.dy1.Service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface GetWorkCountService {
    ResponseEntity<Map<String, Object>> getUserWorksCount(String url);
}