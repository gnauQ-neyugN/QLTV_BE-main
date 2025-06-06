package com.example.web_qltv_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ddc_category")
public class DdcCategory {
    @Id
    @Column(name = "id_ddc_category")
    private int idDdcCategory;
    @Column(name = "name_category")
    private String nameCategory;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "book_ddc_category", joinColumns = @JoinColumn(name = "id_ddc_category"), inverseJoinColumns = @JoinColumn(name = "id_book"))
    private List<Book> listBooks;
}
