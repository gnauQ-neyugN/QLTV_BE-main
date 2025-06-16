package com.example.web_qltv_be.controller;

import com.example.web_qltv_be.dao.BookItemRepository;
import com.example.web_qltv_be.dto.BookItemDTO;
import com.example.web_qltv_be.entity.BookItem;
import com.example.web_qltv_be.service.book.BookService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/book")
public class BookController {
    @Autowired
    private BookService bookService;
    @Autowired
    private BookItemRepository bookItemRepository;

    @PostMapping(path = "/add-book")
    public ResponseEntity<?> save(@RequestBody JsonNode jsonData) {
        try {
            return bookService.save(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi r");
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(path = "/update-book")
    public ResponseEntity<?> update(@RequestBody JsonNode jsonData) {
        try{
            return bookService.update(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi");
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/book-items/available/{bookId}")
    public ResponseEntity<?> getAvailableBookItems(
            @PathVariable int bookId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<BookItem> availableItems = bookItemRepository
                    .findByBookIdAndStatusOrderByIdBookItem(bookId, "Có sẵn",
                            PageRequest.of(0, limit));

            // Chuyển đổi sang DTO
            List<BookItemDTO> bookItemDTOs = availableItems.stream()
                    .map(BookItemDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(bookItemDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching available book items");
        }
    }


    @GetMapping(path = "/get-total")
    public long getTotal() {
        return bookService.getTotalBook();
    }
}
