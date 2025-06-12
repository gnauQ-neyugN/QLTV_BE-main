package com.example.web_qltv_be.service.statistics;

import com.example.web_qltv_be.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private LibraryViolationTypeRepository libraryViolationTypeRepository;

    @Autowired
    private BorrowRecordDetailRepository borrowRecordDetailRepository;

    @Override
    public ResponseEntity<?> getDashboardStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // Tổng số tiền kiếm được từ đơn hàng
            Double totalRevenue = orderRepository.findAll().stream()
                    .filter(order -> "Thành công".equals(order.getStatus()))
                    .mapToDouble(order -> order.getTotalPrice())
                    .sum();

            // Tổng số phiếu mượn
            long totalBorrowRecords = borrowRecordRepository.count();

            // Tổng số hóa đơn (đơn hàng thành công)
            long totalSuccessfulOrders = orderRepository.findAll().stream()
                    .filter(order -> "Thành công".equals(order.getStatus()))
                    .count();

            // Tổng số tài khoản
            long totalUsers = userRepository.count();

            // Tổng số sách
            long totalBooks = bookRepository.count();

            // Tổng số tiền phạt từ phiếu mượn
            Double totalFines = borrowRecordRepository.findAll().stream()
                    .mapToDouble(record -> record.getFineAmount())
                    .sum();

            // Tổng số loại lỗi phạt
            long totalViolationTypes = libraryViolationTypeRepository.count();

            statistics.put("totalRevenue", totalRevenue);
            statistics.put("totalBorrowRecords", totalBorrowRecords);
            statistics.put("totalSuccessfulOrders", totalSuccessfulOrders);
            statistics.put("totalUsers", totalUsers);
            statistics.put("totalBooks", totalBooks);
            statistics.put("totalFines", totalFines);
            statistics.put("totalViolationTypes", totalViolationTypes);

            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy thống kê dashboard");
        }
    }

    @Override
    public ResponseEntity<?> getRevenueStatistics() {
        try {
            Map<String, Object> revenueStats = new HashMap<>();

            // Revenue từ bán sách
            Double bookRevenue = orderRepository.findAll().stream()
                    .filter(order -> "Thành công".equals(order.getStatus()))
                    .mapToDouble(order -> order.getTotalPrice())
                    .sum();

            // Revenue từ phí phạt
            Double fineRevenue = borrowRecordRepository.findAll().stream()
                    .mapToDouble(record -> record.getFineAmount())
                    .sum();

            Double totalRevenue = bookRevenue + fineRevenue;

            revenueStats.put("bookRevenue", bookRevenue);
            revenueStats.put("fineRevenue", fineRevenue);
            revenueStats.put("totalRevenue", totalRevenue);

            return ResponseEntity.ok(revenueStats);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy thống kê doanh thu");
        }
    }

    @Override
    public ResponseEntity<?> getTopBorrowedBooks(int limit) {
        try {
            // Lấy tất cả sách từ cơ sở dữ liệu và sắp xếp theo borrowQuantity
            List<Map<String, Object>> topBorrowedBooks = bookRepository.findAll().stream()
                    .filter(book -> book.getBorrowQuantity() > 0)
                    .sorted((b1, b2) -> Integer.compare(b2.getBorrowQuantity(), b1.getBorrowQuantity()))
                    .limit(limit)
                    .map(book -> {
                        Map<String, Object> bookInfo = new HashMap<>();
                        bookInfo.put("idBook", book.getIdBook());
                        bookInfo.put("nameBook", book.getNameBook());
                        bookInfo.put("author", book.getAuthor());
                        bookInfo.put("borrowQuantity", book.getBorrowQuantity());
                        bookInfo.put("isbn", book.getIsbn());
                        return bookInfo;
                    })
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(topBorrowedBooks);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy top sách được mượn nhiều nhất");
        }
    }

    @Override
    public ResponseEntity<?> getMostCommonViolations(int limit) {
        try {
            // Đếm số lần xuất hiện của mỗi loại vi phạm
            Map<String, Long> violationCounts = new HashMap<>();
            Map<String, Map<String, Object>> violationDetails = new HashMap<>();

            borrowRecordDetailRepository.findAll().stream()
                    .filter(detail -> detail.getViolationType() != null)
                    .forEach(detail -> {
                        String violationCode = detail.getViolationType().getCode();
                        violationCounts.put(violationCode,
                                violationCounts.getOrDefault(violationCode, 0L) + 1);

                        if (!violationDetails.containsKey(violationCode)) {
                            Map<String, Object> violationInfo = new HashMap<>();
                            violationInfo.put("code", detail.getViolationType().getCode());
                            violationInfo.put("description", detail.getViolationType().getDescription());
                            violationInfo.put("fine", detail.getViolationType().getFine());
                            violationDetails.put(violationCode, violationInfo);
                        }
                    });

            // Sắp xếp theo số lần xuất hiện và lấy top
            List<Map<String, Object>> mostCommonViolations = violationCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        Map<String, Object> violationInfo = new HashMap<>(violationDetails.get(entry.getKey()));
                        violationInfo.put("count", entry.getValue());
                        return violationInfo;
                    })
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(mostCommonViolations);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy thống kê vi phạm phổ biến nhất");
        }
    }
}