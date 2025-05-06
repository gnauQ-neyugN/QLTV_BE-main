package com.example.web_bookstore_be.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;

@Data
@Entity
@Table(name = "borrow_record_detail")
public class BorrowRecordDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "quantity")
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "id_borrow_record")
    private BorrowRecord borrowRecord;

    @ManyToOne
    @JoinColumn(name = "id_book")
    private Book book;

    @Column(name = "is_returned")
    private boolean isReturned;

    @Column(name = "return_date")
    private Date returnDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
