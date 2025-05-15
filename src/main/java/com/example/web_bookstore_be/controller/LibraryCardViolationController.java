package com.example.web_bookstore_be.controller;

import com.example.web_bookstore_be.service.librarycardviolation.LibraryCardViolationService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/library-card-violation")
@CrossOrigin("*")
public class LibraryCardViolationController {
    @Autowired
    private LibraryCardViolationService libraryCardViolationService;

    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody JsonNode jsonNode) {
        try {
            return libraryCardViolationService.save(jsonNode);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody JsonNode jsonNode) {
        try {
            return libraryCardViolationService.update(jsonNode);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
