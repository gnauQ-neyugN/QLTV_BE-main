package com.example.web_qltv_be.controller;

import com.example.web_qltv_be.service.statistics.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
@CrossOrigin("*")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStatistics() {
        try {
            return statisticsService.getDashboardStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenueStatistics() {
        try {
            return statisticsService.getRevenueStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/top-borrowed-books")
    public ResponseEntity<?> getTopBorrowedBooks(@RequestParam(defaultValue = "3") int limit) {
        try {
            return statisticsService.getTopBorrowedBooks(limit);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/most-common-violations")
    public ResponseEntity<?> getMostCommonViolations(@RequestParam(defaultValue = "3") int limit) {
        try {
            return statisticsService.getMostCommonViolations(limit);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}