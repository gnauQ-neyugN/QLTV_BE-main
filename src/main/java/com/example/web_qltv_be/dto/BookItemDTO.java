// Tạo DTO class
package com.example.web_qltv_be.dto;

import com.example.web_qltv_be.entity.BookItem;

public class BookItemDTO {
    private int idBookItem;
    private String barcode;
    private String status;
    private String location;
    private int condition;
    private int bookId;
    private String bookName;
    private String author;

    // Constructors
    public BookItemDTO() {}

    public BookItemDTO(BookItem bookItem) {
        this.idBookItem = bookItem.getIdBookItem();
        this.barcode = bookItem.getBarcode();
        this.status = bookItem.getStatus();
        this.location = bookItem.getLocation();
        this.condition = bookItem.getCondition();
        if (bookItem.getBook() != null) {
            this.bookId = bookItem.getBook().getIdBook();
            this.bookName = bookItem.getBook().getNameBook();
            this.author = bookItem.getBook().getAuthor();
        }
    }

    // Getters and Setters
    public int getIdBookItem() { return idBookItem; }
    public void setIdBookItem(int idBookItem) { this.idBookItem = idBookItem; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCondition() { return condition; }
    public void setCondition(int condition) { this.condition = condition; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}