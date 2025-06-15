package com.example.web_qltv_be.dao;

import com.example.web_qltv_be.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.sql.Date;
import java.util.List;

@RepositoryRestResource(path = "borrow-records")
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Integer> {
    Page<BorrowRecord> findBorrowRecordsByLibraryCard_CardNumber(String cardNumber, Pageable pageable);
    public BorrowRecord findFirstByLibraryCard_CardNumber(String cardNumber);
    int countByBorrowDate(Date borrowDate);
    boolean existsByRecordId(String recordId);
    @Query("SELECT br FROM BorrowRecord br WHERE br.recordId = :recordId")
    BorrowRecord findByRecordId(@Param("recordId") String recordId);
}