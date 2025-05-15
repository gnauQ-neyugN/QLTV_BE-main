package com.example.web_bookstore_be.service.borrowrecord;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

public interface BorrowRecordService {
    public ResponseEntity<?> save(JsonNode jsonNode);
    public ResponseEntity<?> update(JsonNode jsonNode);
    public ResponseEntity<?> cancel(JsonNode jsonNode);
    public ResponseEntity<?> getByLibraryCard(String cardNumber);
    public ResponseEntity<?> return1Book(JsonNode jsonNode);
}
