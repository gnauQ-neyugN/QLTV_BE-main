package com.example.web_qltv_be.dao;

import com.example.web_qltv_be.entity.Book;
import com.example.web_qltv_be.entity.BookItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(path = "book-items")
public interface BookItemRepository extends JpaRepository<BookItem, Integer> {
    int countByBook(Book book);
    @Query("SELECT bi FROM BookItem bi WHERE bi.book.idBook = :bookId AND bi.status = :status ORDER BY bi.idBookItem")
    List<BookItem> findByBookIdAndStatusOrderByIdBookItem(
            @Param("bookId") int bookId,
            @Param("status") String status,
            Pageable pageable
    );
    BookItem findByBarcode(@Param("barcode") String barcode);
    @Query("SELECT bi FROM BookItem bi WHERE LOWER(bi.barcode) LIKE LOWER(CONCAT('%', :barcode, '%')) AND (bi.status = 'Có sẵn' OR bi.status = 'AVAILABLE') ORDER BY bi.barcode")
    List<BookItem> findAvailableByBarcodeContaining(@Param("barcode") String barcode);

    List<BookItem> findByBook(Book book);
}