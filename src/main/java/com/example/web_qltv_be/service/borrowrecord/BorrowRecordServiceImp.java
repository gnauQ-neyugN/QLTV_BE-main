package com.example.web_qltv_be.service.borrowrecord;

import com.example.web_qltv_be.dao.*;
import com.example.web_qltv_be.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowRecordServiceImp implements BorrowRecordService {
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private LibraryCardRepository libraryCardRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private LibraryViolationTypeRepository libraryViolationTypeRepository;
    private final ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BorrowRecordDetailRepository borrowRecordDetailRepository;
    @Autowired
    private BookItemRepository bookItemRepository;

    public BorrowRecordServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<?> save(JsonNode jsonNode) {
        try {
            BorrowRecord borrowRecordData = objectMapper.treeToValue(jsonNode, BorrowRecord.class);
            borrowRecordData.setBorrowDate(Date.valueOf(LocalDate.now()));
            borrowRecordData.setDueDate(Date.valueOf(LocalDate.now().plusDays(60)));
            borrowRecordData.setStatus("Đang xử lý");

            int idLibraryCard = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
            Optional<LibraryCard> libraryCard = libraryCardRepository.findById(idLibraryCard);

            if (libraryCard.isEmpty()) {
                return ResponseEntity.badRequest().body(new Notification("Thẻ thư viện không tồn tại"));
            }

            if (!libraryCard.get().isActivated()) {
                return ResponseEntity.badRequest().body(new Notification("Thẻ thư viện chưa được kích hoạt"));
            }

            borrowRecordData.setLibraryCard(libraryCard.get());
            borrowRecordData.setRecordId(generateRecordId());
            // ✅ Save only once
            BorrowRecord savedBorrowRecord = borrowRecordRepository.save(borrowRecordData);

            JsonNode jsonData = jsonNode.get("bookItem");
            for (JsonNode itemNode : jsonData) {
                int idBookItem = itemNode.get("idBookItem").asInt();
                Optional<BookItem> bookItemOptional = bookItemRepository.findById(idBookItem);

                if (bookItemOptional.isEmpty()) {
                    return ResponseEntity.badRequest().body(new Notification("Không tìm thấy bản sao sách (BookItem) với ID: " + idBookItem));
                }

                BookItem bookItem = bookItemOptional.get();

                if (!bookItem.getStatus().equals("Có sẵn")) {
                    return ResponseEntity.badRequest().body(new Notification("Bản sao sách '" + bookItem.getBarcode() + "' không sẵn sàng để mượn"));
                }
                bookItem.setStatus("Đặt mượn");
                bookItemRepository.save(bookItem);
                BorrowRecordDetail detail = new BorrowRecordDetail();
                detail.setBorrowRecord(savedBorrowRecord);
                detail.setBookItem(bookItem);
                detail.setReturned(false);
                detail.setQuantity(1);

                borrowRecordDetailRepository.save(detail);

            }

            return ResponseEntity.ok(new Notification("Mượn sách thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Notification("Lỗi khi mượn sách: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(JsonNode jsonNode) {
        try {
            int idBorrowRecord = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idBorrowRecord"))));
            String status = formatStringByJson(String.valueOf(jsonNode.get("status")));

            Optional<BorrowRecord> borrowRecordOpt = borrowRecordRepository.findById(idBorrowRecord);
            if (borrowRecordOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(new Notification("Không tìm thấy phiếu mượn"));
            }

            BorrowRecord borrowRecord = borrowRecordOpt.get();
            borrowRecord.setStatus(status);

            List<BorrowRecordDetail> borrowRecordDetailList = borrowRecordDetailRepository.findBorrowRecordDetailByBorrowRecord(borrowRecord);

            // Nếu cập nhật sang trạng thái "Đã trả"
            if (status.equals("Đã trả")) {
                borrowRecord.setReturnDate(Date.valueOf(LocalDate.now()));
                double fineAmount = 0;

                for (BorrowRecordDetail detail : borrowRecordDetailList) {
                    if (detail.isReturned()) {
                        BookItem bookItem = detail.getBookItem();
                        LibraryViolationType violationType = detail.getViolationType();

                        if (violationType != null) {
                            if (violationType.getCode().equals("Trả muộn")) {
                                fineAmount += violationType.getFine();
                            } else if (violationType.getCode().equals("Làm mất sách")) {
                                fineAmount += bookItem.getCondition() * bookItem.getBook().getListPrice() / 100.0;
                            }
                        }
                    }
                }

                borrowRecord.setFineAmount(fineAmount);
            }

            // Nếu cập nhật sang trạng thái "Đang mượn"
            if (status.equals("Đang mượn")) {
                for (BorrowRecordDetail detail : borrowRecordDetailList) {
                    if (!detail.isReturned()) {
                        BookItem bookItem = detail.getBookItem();
                        Book book = bookItem.getBook();

                        bookItem.setStatus("BORROWED");
                        bookItemRepository.save(bookItem);

                        book.setQuantityForBorrow(book.getQuantityForBorrow() - 1);
                        book.setBorrowQuantity(book.getBorrowQuantity() + 1);
                        bookRepository.save(book);
                    }
                }
            }

            borrowRecordRepository.save(borrowRecord);
            return ResponseEntity.ok(new Notification("Cập nhật phiếu mượn thành công"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Notification("Lỗi khi cập nhật phiếu mượn: " + e.getMessage()));
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> cancel(JsonNode jsonNode) {
        try {
            int idBorrowRecord = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idBorrowRecord"))));
            Optional<BorrowRecord> borrowRecordOpt = borrowRecordRepository.findById(idBorrowRecord);

            if (borrowRecordOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(new Notification("Phiếu mượn không tồn tại"));
            }

            BorrowRecord borrowRecord = borrowRecordOpt.get();

            if (!borrowRecord.getStatus().equals("Đang xử lý")) {
                return ResponseEntity.badRequest().body(new Notification("Không thể hủy phiếu mượn với trạng thái hiện tại"));
            }

            borrowRecord.setStatus("Hủy");
            borrowRecordRepository.save(borrowRecord);

            return ResponseEntity.ok(new Notification("Hủy phiếu mượn thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Notification("Lỗi khi hủy phiếu mượn: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getByLibraryCard(String cardNumber) {
        try {
            Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id"));
            Page<BorrowRecord> borrowRecords = borrowRecordRepository
                    .findBorrowRecordsByLibraryCard_CardNumber(cardNumber, pageable);

            return ResponseEntity.ok(borrowRecords.getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Notification("Lỗi khi tìm phiếu mượn: " + e.getMessage()));
        }
    }



    @Override
    public ResponseEntity<?> return1Book(JsonNode jsonNode) {
        try {
            int id = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("id"))));
            String violationCode = formatStringByJson(String.valueOf(jsonNode.get("code")));
            String returnDateStr = jsonNode.get("returnDate").asText(null);
            String notes = jsonNode.get("notes").asText(null);

            Optional<BorrowRecordDetail> detailOpt = borrowRecordDetailRepository.findById(id);
            Optional<LibraryViolationType> violationTypeOpt = libraryViolationTypeRepository.findByCode(violationCode);

            if (detailOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy chi tiết phiếu mượn");
            }

            BorrowRecordDetail detail = detailOpt.get();

            detail.setViolationType(violationTypeOpt.orElse(null));
            detail.setReturned(true);
            detail.setNotes(notes);
            BookItem bookItem = detail.getBookItem();

            if (violationCode.equals("Làm mất sách")) {
                bookItem.setStatus("Đã mất");
            } else {
                bookItem.setStatus("Có sẵn");
                bookItem.setCondition(bookItem.getCondition() - 1);
                Book book = bookItem.getBook();
                book.setBorrowQuantity(book.getBorrowQuantity() - 1);
                book.setQuantityForBorrow(book.getQuantityForBorrow() + 1);
                bookRepository.save(book);
            }

            if (returnDateStr != null) {
                detail.setReturnDate(Date.valueOf(LocalDate.parse(returnDateStr)));
            } else {
                detail.setReturnDate(Date.valueOf(LocalDate.now()));
            }
            bookItemRepository.save(bookItem);
            borrowRecordDetailRepository.save(detail);
            return ResponseEntity.ok().body("Trả sách thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Không thể trả sách: " + e.getMessage());
        }
    }

    private String generateRecordId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int retry = 0;
        String recordId;

        while (true) {
            String serial = String.format("%04d", borrowRecordRepository.countByBorrowDate(Date.valueOf(LocalDate.now())) + retry + 1);
            recordId = "BR-" + datePart + "-" + serial;

            // Kiểm tra xem recordId này đã tồn tại chưa
            if (!borrowRecordRepository.existsByRecordId(recordId)) {
                break;
            }
            retry++;
        }

        return recordId;
    }


    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}