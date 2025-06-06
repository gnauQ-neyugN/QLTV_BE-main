package com.example.web_qltv_be.controller;

import com.example.web_qltv_be.service.librarycard.LibraryCardService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/library-card")
@CrossOrigin("*")
public class LibraryCardController {

    @Autowired
    private LibraryCardService libraryCardService;

    @PutMapping("/create")
    public ResponseEntity<?> save(@RequestBody JsonNode jsonData) {
        try {
            return libraryCardService.save(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody JsonNode jsonData) {
        try {
            return libraryCardService.update(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/deactivate")
    public ResponseEntity<?> delete(@RequestBody JsonNode jsonData) {
        try {
            return libraryCardService.delete(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getByUserId(@PathVariable int userId) {
        try {
            return libraryCardService.getByUserId(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/sendRequestRenewCard")
    public ResponseEntity<?> sendRequestRenewCard(@RequestBody JsonNode jsonData) {
        try {
            return libraryCardService.sendRequestRenewCard(jsonData);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/renew")
    public ResponseEntity<?> renewCard(@RequestBody JsonNode jsonData) {
        try {
            return libraryCardService.renewCard(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{cardNumber}")
    public ResponseEntity<?> getCardStatus(@PathVariable String cardNumber) {
        try {
            return libraryCardService.getCardStatus(cardNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "idLibraryCard") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return libraryCardService.getAllCards(pageable, status);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}