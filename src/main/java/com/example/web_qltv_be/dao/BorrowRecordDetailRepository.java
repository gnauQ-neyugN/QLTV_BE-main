package com.example.web_qltv_be.dao;

import com.example.web_qltv_be.entity.BorrowRecord;
import com.example.web_qltv_be.entity.BorrowRecordDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "borrow-record-detail")
public interface BorrowRecordDetailRepository extends JpaRepository<BorrowRecordDetail, Integer> {
    public List<BorrowRecordDetail> findBorrowRecordDetailByBorrowRecord(BorrowRecord borrowRecord);
}