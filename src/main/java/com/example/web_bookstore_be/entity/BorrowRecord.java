package com.example.web_bookstore_be.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
@Entity
@Table(name = "borrow_record")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "borrow_date")
    private Date borrowDate;

    @Column(name = "due_date")
    private Date dueDate;

    @Column(name = "return_date")
    private Date returnDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "borrowRecord", cascade = CascadeType.ALL)
    private List<BorrowRecordDetail> borrowRecordDetails;

    @ManyToOne
    @JoinColumn(name = "id_libary_card")
    private LibraryCard libraryCard;
}
