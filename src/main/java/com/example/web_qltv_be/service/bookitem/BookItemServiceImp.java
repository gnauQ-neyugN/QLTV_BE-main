package com.example.web_qltv_be.service.bookitem;

import com.example.web_qltv_be.dao.BookItemRepository;
import com.example.web_qltv_be.entity.BookItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BookItemServiceImp implements BookItemService {
    @Autowired
    private BookItemRepository bookItemRepository;

    private final ObjectMapper objectMapper;

    public BookItemServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public ResponseEntity<?> update(JsonNode json) {
        try {
            BookItem updatedItem = objectMapper.treeToValue(json, BookItem.class);
            BookItem existingItem = bookItemRepository.findById(updatedItem.getIdBookItem())
                    .orElse(null);
            if (existingItem == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy BookItem với ID: " + updatedItem.getIdBookItem());
            }
            existingItem.setLocation(updatedItem.getLocation());
            bookItemRepository.save(existingItem);
            return ResponseEntity.ok(existingItem);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi khi cập nhật BookItem");
        }
    }

    @Override
    public ResponseEntity<?> delete(JsonNode json) {
        try {
            int idBookItem = json.get("idBookItem").asInt();
            BookItem existingItem = bookItemRepository.findById(idBookItem).orElse(null);
            if (existingItem == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy BookItem với ID: " + idBookItem);
            }
            bookItemRepository.delete(existingItem);
            return ResponseEntity.ok("Đã xóa BookItem với ID: " + idBookItem);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi khi xóa BookItem");
        }
    }

}
