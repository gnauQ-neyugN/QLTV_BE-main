package com.example.web_bookstore_be.controller;

import com.example.web_bookstore_be.service.borrowrecord.BorrowRecordService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/borrow-record")
public class BorrowController {
    @Autowired
    private BorrowRecordService borrowRecordService;

    @PostMapping("/add-borrow-record")
    public ResponseEntity<?> save(@RequestBody JsonNode jsonData) {
        try {
            return borrowRecordService.save(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update-borrow-record")
    public ResponseEntity<?> update(@RequestBody JsonNode jsonData) {
        try {
            return borrowRecordService.update(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/cancel-borrow-record")
    public ResponseEntity<?> cancel(@RequestBody JsonNode jsonData) {
        try {
            return borrowRecordService.cancel(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get-by-library-card/{cardNumber}")
    public ResponseEntity<?> getByLibraryCard(@PathVariable String cardNumber) {
        try {
            // Get all borrow records for a specific library card
            return ResponseEntity.ok(borrowRecordService.getByLibraryCard(cardNumber));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/return-1-book")
    public ResponseEntity<?> return1Book(@RequestBody JsonNode jsonData) {
        try {
            return borrowRecordService.return1Book(jsonData);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}