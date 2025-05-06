package com.example.web_bookstore_be.dao;

import com.example.web_bookstore_be.entity.BorrowRecord;
import com.example.web_bookstore_be.entity.LibraryCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@RepositoryRestResource(path = "borrow-records")
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Integer> {
    public List<BorrowRecord> findBorrowRecordsByLibraryCard_CardNumber(String cardNumber);
    public BorrowRecord findFirstByLibraryCard_CardNumber(String cardNumber);
}