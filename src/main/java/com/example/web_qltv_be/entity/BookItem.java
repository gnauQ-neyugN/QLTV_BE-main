package com.example.web_qltv_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book_item")
public class BookItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_book_item")
    private int idBookItem;

    @Column(name = "barcode", unique = true)
    private String barcode; // Mã vạch/định danh duy nhất từng quyển sách

    @Column(name = "status")
    private String status; // Trạng thái

    @Column(name = "location")
    private String location; // Vị trí quyển sách

    @Column(name = "book_condition")
    private int condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_book")
    private Book book; // Liên kết đến sách gốc
}
