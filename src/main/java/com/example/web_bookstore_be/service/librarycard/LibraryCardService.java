package com.example.web_bookstore_be.service.librarycard;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

public interface LibraryCardService {
    public ResponseEntity<?> save(JsonNode jsonNode);
    public ResponseEntity<?> update(JsonNode jsonNode);
    public ResponseEntity<?> delete(JsonNode jsonNode);
}
