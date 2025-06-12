package com.example.web_qltv_be.dao;

import com.example.web_qltv_be.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.sql.Date;
import java.util.List;

@RepositoryRestResource(path = "borrow-records")
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Integer> {
    public List<BorrowRecord> findBorrowRecordsByLibraryCard_CardNumber(String cardNumber);
    public BorrowRecord findFirstByLibraryCard_CardNumber(String cardNumber);
    int countByBorrowDate(Date borrowDate);
    boolean existsByRecordId(String recordId);
}