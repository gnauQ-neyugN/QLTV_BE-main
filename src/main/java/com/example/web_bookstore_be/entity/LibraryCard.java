package com.example.web_bookstore_be.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
@Entity
@Table(name = "library_card")
public class LibraryCard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_library_card") //
  private int idLibraryCard;

  @Column(name = "card_number")
  private String cardNumber;

  @Column(name = "activated")
  private boolean activated;

  @Column(name = "issued_date")
  private Date issuedDate;

  @OneToOne(mappedBy = "libraryCard")
  private User user;

  @ManyToMany
  @JoinTable(
          name = "library_card_violation",
          joinColumns = @JoinColumn(name = "id_library_card", referencedColumnName = "id_library_card"),
          inverseJoinColumns = @JoinColumn(name = "id_library_violation_type", referencedColumnName = "id_library_violation_type")
  )
  private List<LibraryViolationType> violationTypes;

  @OneToMany(mappedBy = "libraryCard", cascade = CascadeType.ALL)
  private List<BorrowRecord> borrowRecords;

  @Override
  public String toString() {
    return "LibraryCard{" +
            "idLibraryCard=" + idLibraryCard +
            ", cardNumber='" + cardNumber + '\'' +
            ", issuedDate=" + issuedDate +
            '}';
  }
}
