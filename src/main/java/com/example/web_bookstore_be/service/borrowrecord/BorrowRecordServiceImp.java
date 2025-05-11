package com.example.web_bookstore_be.service.borrowrecord;

import com.example.web_bookstore_be.dao.*;
import com.example.web_bookstore_be.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
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
    private BorrowRecordDetailRepository recordDetailRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    private final ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    public BorrowRecordServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<?> save(JsonNode jsonNode) {
        try {
            BorrowRecord borrowRecordData = objectMapper.treeToValue(jsonNode, BorrowRecord.class);
            borrowRecordData.setBorrowDate(Date.valueOf(LocalDate.now()));

            // Default due date is 14 days from now
            LocalDate dueDate = LocalDate.now().plusDays(60);
            borrowRecordData.setDueDate(Date.valueOf(dueDate));

            borrowRecordData.setStatus("Đang xử lý");

            int idLibraryCard = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
            Optional<LibraryCard> libraryCard = libraryCardRepository.findById(idLibraryCard);
            Optional<User> user = userRepository.findUsersByLibraryCard_IdLibraryCard(idLibraryCard);

            if (libraryCard.isEmpty()) {
                return ResponseEntity.badRequest().body(new Notification("Thẻ thư viện không tồn tại"));
            }

            if (!libraryCard.get().isActivated()) {
                return ResponseEntity.badRequest().body(new Notification("Thẻ thư viện chưa được kích hoạt"));
            }

            borrowRecordData.setLibraryCard(libraryCard.get());

            BorrowRecord newBorrowRecord = borrowRecordRepository.save(borrowRecordData);

            JsonNode jsonData = jsonNode.get("book");
            for (JsonNode bookNode : jsonData) {
                int quantity = Integer.parseInt(formatStringByJson(String.valueOf(bookNode.get("quantity"))));
                Book bookResponse = objectMapper.treeToValue(bookNode.get("book"), Book.class);
                Optional<Book> book = bookRepository.findById(bookResponse.getIdBook());

                if (book.isEmpty()) {
                    return ResponseEntity.badRequest().body(new Notification("Sách không tồn tại"));
                }

                if (book.get().getQuantity() < quantity) {
                    return ResponseEntity.badRequest().body(new Notification("Số lượng sách không đủ"));
                }

                book.get().setQuantity(book.get().getQuantity() - quantity);
                book.get().setBorrowQuantity(book.get().getBorrowQuantity() + quantity);

                BorrowRecordDetail borrowRecordDetail = new BorrowRecordDetail();
                borrowRecordDetail.setBook(book.get());
                borrowRecordDetail.setQuantity(quantity);
                borrowRecordDetail.setBorrowRecord(newBorrowRecord);
                borrowRecordDetail.setReturned(false);
                recordDetailRepository.save(borrowRecordDetail);
                bookRepository.save(book.get());
            }

            cartItemRepository.deleteCartItemsByIdUser(user.get().getIdUser());

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

            // If the record is marked as 'Đã trả'
            if (status.equals("Đã trả")) {
                borrowRecord.setReturnDate(Date.valueOf(LocalDate.now()));
                List<BorrowRecordDetail> borrowRecordDetailList = recordDetailRepository.findBorrowRecordDetailByBorrowRecord(borrowRecord);

                for (BorrowRecordDetail borrowRecordDetail : borrowRecordDetailList) {
                    if (!borrowRecordDetail.isReturned()) {
                        Book bookBorrowRecordDetail = borrowRecordDetail.getBook();
                        bookBorrowRecordDetail.setBorrowQuantity(bookBorrowRecordDetail.getBorrowQuantity() - borrowRecordDetail.getQuantity());
                        bookBorrowRecordDetail.setQuantity(bookBorrowRecordDetail.getQuantity() + borrowRecordDetail.getQuantity());
                        bookRepository.save(bookBorrowRecordDetail);

                        borrowRecordDetail.setReturned(true);
                        borrowRecordDetail.setReturnDate(Date.valueOf(LocalDate.now()));
                        recordDetailRepository.save(borrowRecordDetail);
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

            List<BorrowRecordDetail> borrowRecordDetailList = recordDetailRepository.findBorrowRecordDetailByBorrowRecord(borrowRecord);
            for (BorrowRecordDetail borrowRecordDetail : borrowRecordDetailList) {
                Book book = borrowRecordDetail.getBook();
                book.setBorrowQuantity(book.getBorrowQuantity() - borrowRecordDetail.getQuantity());
                book.setQuantity(book.getQuantity() + borrowRecordDetail.getQuantity());
                bookRepository.save(book);
            }

            return ResponseEntity.ok(new Notification("Hủy phiếu mượn thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Notification("Lỗi khi hủy phiếu mượn: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getByLibraryCard(String cardNumber) {
        try {
            List<BorrowRecord> borrowRecords = borrowRecordRepository.findBorrowRecordsByLibraryCard_CardNumber(cardNumber);
            return ResponseEntity.ok(borrowRecords);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Notification("Lỗi khi tìm phiếu mượn: " + e.getMessage()));
        }
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}