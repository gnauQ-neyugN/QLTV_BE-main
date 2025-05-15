package com.example.web_bookstore_be.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "library_violation_type")
public class LibraryViolationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_library_violation_type")
    private int idLibraryViolationType;

    @Column(name = "code", unique = true, nullable = false)
    private String code; // Ví dụ: LATE_RETURN, LOST_BOOK

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "fine") // tiền phạt
    private double fine;

    @ManyToMany(mappedBy = "violationTypes")
    private List<LibraryCard> libraryCards;
}
