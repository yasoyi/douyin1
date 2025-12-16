package com.alan.dy1.Service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AudioService {
    ResponseEntity<Map<String, Object>> downloadAndConvertAudio(String url);
}