package com.example.web_qltv_be.service.bookitem;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

public interface BookItemService {
    public ResponseEntity<?> update(JsonNode json);
    public ResponseEntity<?> delete(JsonNode json);
}
