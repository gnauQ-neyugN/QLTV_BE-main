package com.example.web_qltv_be.controller;

import com.example.web_qltv_be.service.bookitem.BookItemService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book-item")
public class BookItemController {
    @Autowired
    private BookItemService bookItemService;

    @PutMapping(path = "/update-book-item")
    public ResponseEntity<?> update(@RequestBody JsonNode jsonNode) {
        try {
            return bookItemService.update(jsonNode);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping(path = "/delete-book-item")
    public ResponseEntity<?> delete(@RequestBody JsonNode jsonNode) {
        try {
            return bookItemService.delete(jsonNode);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
