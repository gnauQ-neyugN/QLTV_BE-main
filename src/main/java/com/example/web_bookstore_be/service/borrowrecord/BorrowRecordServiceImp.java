package com.example.web_bookstore_be.service.borrowrecord;

import com.example.web_bookstore_be.dao.BookRepository;
import com.example.web_bookstore_be.dao.BorrowRecordDetailRepository;
import com.example.web_bookstore_be.dao.BorrowRecordRepository;
import com.example.web_bookstore_be.dao.LibraryCardRepository;
import com.example.web_bookstore_be.entity.Book;
import com.example.web_bookstore_be.entity.BorrowRecord;
import com.example.web_bookstore_be.entity.BorrowRecordDetail;
import com.example.web_bookstore_be.entity.LibraryCard;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    public final ObjectMapper objectMapper;

    public BorrowRecordServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public ResponseEntity<?> save(JsonNode jsonNode) {
        try {
            BorrowRecord borrowRecordData = objectMapper.treeToValue(jsonNode, BorrowRecord.class);
            borrowRecordData.setBorrowDate(Date.valueOf(LocalDate.now()));
            borrowRecordData.setStatus("Đang xử lý");

            int idLibraryCard = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
            Optional<LibraryCard> libraryCard = libraryCardRepository.findById(idLibraryCard);

            borrowRecordData.setLibraryCard(libraryCard.get());

            BorrowRecord newBorrowRecord = borrowRecordRepository.save(borrowRecordData);

            JsonNode jsonData = jsonNode.get("book");
            for (JsonNode bookNode : jsonData) {
                int quantity = Integer.parseInt(formatStringByJson(String.valueOf(bookNode.get("quantity"))));
                Book bookResponse = objectMapper.treeToValue(bookNode.get("book"), Book.class);
                Optional<Book> book = bookRepository.findById(bookResponse.getIdBook());

                book.get().setQuantity(book.get().getQuantity() - quantity);
                book.get().setBorrowQuantity(book.get().getBorrowQuantity() + quantity);

                BorrowRecordDetail borrowRecordDetail = new BorrowRecordDetail();
                borrowRecordDetail.setBook(book.get());
                borrowRecordDetail.setQuantity(quantity);
                borrowRecordDetail.setBorrowRecord(newBorrowRecord);
                recordDetailRepository.save(borrowRecordDetail);
                bookRepository.save(book.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> update(JsonNode jsonNode) {
        try {
            int idBorrowRecord = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idBorrowRecord"))));
            String status = formatStringByJson(String.valueOf(jsonNode.get("status")));
            Optional<BorrowRecord> borrowRecord = borrowRecordRepository.findById(idBorrowRecord);
            borrowRecord.get().setStatus(status);

            if (status.equals("Hủy")) {
                List<BorrowRecordDetail> borrowRecordDetailList = recordDetailRepository.findBorrowRecordDetailByBorrowRecord(borrowRecord.get());
                for (BorrowRecordDetail borrowRecordDetail : borrowRecordDetailList) {
                    Book bookBorrowRecordDetail = borrowRecordDetail.getBook();
                    bookBorrowRecordDetail.setBorrowQuantity(bookBorrowRecordDetail.getBorrowQuantity() - borrowRecordDetail.getQuantity());
                    bookBorrowRecordDetail.setQuantity(bookBorrowRecordDetail.getQuantity() + borrowRecordDetail.getQuantity());
                    bookRepository.save(bookBorrowRecordDetail);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> cancel(JsonNode jsonNode) {
        try {
            int idLibraryCard = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
            LibraryCard libraryCard = libraryCardRepository.findById(idLibraryCard).get();

            BorrowRecord borrowRecord = borrowRecordRepository.findFirstByLibraryCard_CardNumber(String.valueOf(libraryCard.getCardNumber()));

            borrowRecord.setStatus("Hủy");

            List<BorrowRecordDetail> borrowRecordDetailList = recordDetailRepository.findBorrowRecordDetailByBorrowRecord(borrowRecord);
            for (BorrowRecordDetail borrowRecordDetail : borrowRecordDetailList) {
                Book bookBorrowRecordDetail = borrowRecordDetail.getBook();
                bookBorrowRecordDetail.setBorrowQuantity(bookBorrowRecordDetail.getBorrowQuantity() - borrowRecordDetail.getQuantity());
                bookBorrowRecordDetail.setQuantity(bookBorrowRecordDetail.getQuantity() + borrowRecordDetail.getQuantity());
                bookRepository.save(bookBorrowRecordDetail);
            }
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}
