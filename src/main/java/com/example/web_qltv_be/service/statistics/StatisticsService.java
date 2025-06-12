package com.example.web_qltv_be.service.statistics;

import org.springframework.http.ResponseEntity;

public interface StatisticsService {
    ResponseEntity<?> getDashboardStatistics();
    ResponseEntity<?> getRevenueStatistics();
    ResponseEntity<?> getTopBorrowedBooks(int limit);
    ResponseEntity<?> getMostCommonViolations(int limit);
}