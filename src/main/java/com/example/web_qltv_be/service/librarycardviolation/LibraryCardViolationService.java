package com.example.web_qltv_be.service.librarycardviolation;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

public interface LibraryCardViolationService {
    public ResponseEntity<?> save (JsonNode jsonNode);
    public ResponseEntity<?> update(JsonNode jsonNode);
}
